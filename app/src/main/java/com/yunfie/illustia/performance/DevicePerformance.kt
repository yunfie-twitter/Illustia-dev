package com.yunfie.illustia.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** Process-wide, versioned device performance decision. Initialize from Application.onCreate. */
object DevicePerformance {
    private const val PREFERENCES_NAME = "device_performance_profile"
    private const val KEY_VERSION_CODE = "version_code"
    private const val KEY_BUILD_FINGERPRINT = "build_fingerprint"
    private const val KEY_TIER = "tier"
    private const val KEY_SCORE = "score"
    private const val LOAD_SAMPLE_INTERVAL_MS = 1_000L
    private val monitorStarted = AtomicBoolean(false)
    private val appForeground = MutableStateFlow(false)
    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _imageQualityCap = MutableStateFlow(AdaptiveImageQuality.HIGH)
    val imageQualityCap: StateFlow<AdaptiveImageQuality> = _imageQualityCap.asStateFlow()
    private val _prefetchAllowed = MutableStateFlow(true)
    val prefetchAllowed: StateFlow<Boolean> = _prefetchAllowed.asStateFlow()
    private val _runtimePolicy =
        MutableStateFlow(
            AdaptiveRuntimePolicy(
                level = RuntimePressureLevel.NORMAL,
                pressureScore = 0,
                imageQualityCap = AdaptiveImageQuality.HIGH,
                imageDecodeParallelism = 2,
                networkRequestParallelism = 6,
                prefetchEnabled = true,
                animatedMediaEnabled = true,
                subtleAnimationsEnabled = true,
                backgroundWorkMultiplier = 1,
            ),
        )
    val runtimePolicy: StateFlow<AdaptiveRuntimePolicy> = _runtimePolicy.asStateFlow()
    private val frameCount = AtomicLong(0L)
    private val slowFrameCount = AtomicLong(0L)
    private val latestScrollVelocity = AtomicLong(0L)
    @Volatile private var thermalStatus = PowerManager.THERMAL_STATUS_NONE
    @Volatile private var pressureController = RuntimePressureController()

    @Volatile
    var profile: DevicePerformanceProfile =
        DevicePerformanceClassifier.classify(
            DevicePerformanceMetrics(
                lowRamDevice = false,
                totalRamMb = 4_096,
                memoryClassMb = 256,
                cpuCoreCount = 8,
                cpuMaxFrequencyMhz = null,
                is64Bit = true,
                sdkInt = Build.VERSION.SDK_INT,
                displayMegapixels = 2.0,
            ),
        )
        private set

    fun initialize(context: Context): DevicePerformanceProfile {
        val appContext = context.applicationContext
        val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val versionCode = currentVersionCode(appContext)
        val fingerprint = Build.FINGERPRINT.orEmpty()
        val cachedTier = preferences.getString(KEY_TIER, null)?.let { runCatching { DevicePerformanceTier.valueOf(it) }.getOrNull() }
        val canReuse =
            cachedTier != null &&
                preferences.getLong(KEY_VERSION_CODE, -1L) == versionCode &&
                preferences.getString(KEY_BUILD_FINGERPRINT, null) == fingerprint

        profile =
            if (canReuse) {
                val cachedScore = preferences.getInt(KEY_SCORE, 8)
                // Rebuild policy values from code so tuning changes are applied after an app update.
                DevicePerformanceClassifier.profileFor(checkNotNull(cachedTier), cachedScore)
            } else {
                val metrics = measure(appContext)
                DevicePerformanceClassifier.classify(metrics).also { measuredProfile ->
                    preferences
                        .edit()
                        .putLong(KEY_VERSION_CODE, versionCode)
                        .putString(KEY_BUILD_FINGERPRINT, fingerprint)
                        .putString(KEY_TIER, measuredProfile.tier.name)
                        .putInt(KEY_SCORE, measuredProfile.score)
                        .putBoolean("low_ram", metrics.lowRamDevice)
                        .putInt("total_ram_mb", metrics.totalRamMb)
                        .putInt("memory_class_mb", metrics.memoryClassMb)
                        .putInt("cpu_cores", metrics.cpuCoreCount)
                        .putInt("cpu_max_mhz", metrics.cpuMaxFrequencyMhz ?: -1)
                        .putBoolean("is_64_bit", metrics.is64Bit)
                        .putInt("sdk_int", metrics.sdkInt)
                        .putLong("display_megapixels_x100", (metrics.displayMegapixels * 100).toLong())
                        .apply()
                }
            }
        val normalCap = normalImageQualityCap(profile.tier)
        pressureController = RuntimePressureController()
        _imageQualityCap.value = normalCap
        _runtimePolicy.value = buildRuntimePolicy(RuntimePressureLevel.NORMAL, 0)
        startLoadMonitor(appContext)
        registerThermalListener(appContext)
        return profile
    }

    fun reportFrame(isJank: Boolean) {
        frameCount.incrementAndGet()
        if (isJank) slowFrameCount.incrementAndGet()
    }

    fun reportScrollVelocity(pixelsPerSecond: Double) {
        latestScrollVelocity.set(pixelsPerSecond.coerceAtLeast(0.0).toLong())
    }

    fun setAppForeground(foreground: Boolean) {
        appForeground.value = foreground
        if (!foreground) latestScrollVelocity.set(0L)
    }

    fun onMemoryPressure() {
        _prefetchAllowed.value = false
        val level = pressureController.requestImmediateElevation(android.os.SystemClock.elapsedRealtime())
        publishRuntimePolicy(level, 100)
    }

    private fun startLoadMonitor(context: Context) {
        if (!monitorStarted.compareAndSet(false, true)) return
        monitorScope.launch {
            var previousCpu = readCpuTimes()
            var previousAppCpuMs = android.os.Process.getElapsedCpuTime()
            var previousSampleAtMs = android.os.SystemClock.elapsedRealtime()
            val memoryInfo = ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val powerManager = context.getSystemService(PowerManager::class.java)
            var smoothedPressure = 0.0
            var thermalHeadroom = 0.0f
            var samplesSinceThermalHeadroom = 10
            var prefetchStableSamples = 0

            while (isActive) {
                if (!appForeground.value) {
                    appForeground.first { it }
                    previousCpu = readCpuTimes()
                    previousAppCpuMs = android.os.Process.getElapsedCpuTime()
                    previousSampleAtMs = android.os.SystemClock.elapsedRealtime()
                }
                delay(LOAD_SAMPLE_INTERVAL_MS)
                if (!appForeground.value) continue
                val currentCpu = readCpuTimes()
                val systemCpuLoad = cpuLoad(previousCpu, currentCpu)
                previousCpu = currentCpu ?: previousCpu
                val sampledAtMs = android.os.SystemClock.elapsedRealtime()
                val appCpuMs = android.os.Process.getElapsedCpuTime()
                val wallDeltaMs = (sampledAtMs - previousSampleAtMs).coerceAtLeast(1L)
                val appCpuLoad = ((appCpuMs - previousAppCpuMs).toDouble() / wallDeltaMs).coerceIn(0.0, 1.0)
                previousAppCpuMs = appCpuMs
                previousSampleAtMs = sampledAtMs
                activityManager.getMemoryInfo(memoryInfo)
                val availableMemoryRatio =
                    if (memoryInfo.totalMem > 0L) memoryInfo.availMem.toDouble() / memoryInfo.totalMem else 0.0
                val memoryHeadroom =
                    if (memoryInfo.totalMem > 0L) {
                        ((memoryInfo.availMem - memoryInfo.threshold).toDouble() / memoryInfo.totalMem).coerceIn(0.0, 1.0)
                    } else {
                        0.0
                    }
                val frames = frameCount.getAndSet(0L)
                val slowFrames = slowFrameCount.getAndSet(0L)
                val jankRatio = if (frames >= 10L) slowFrames.toDouble() / frames else 0.0
                if (Build.VERSION.SDK_INT >= 30 && samplesSinceThermalHeadroom++ >= 10) {
                    thermalHeadroom = powerManager.getThermalHeadroom(0).takeIf { it.isFinite() } ?: thermalHeadroom
                    samplesSinceThermalHeadroom = 0
                }
                val rawPressure =
                    calculatePressureScore(
                        systemCpuLoad = systemCpuLoad,
                        appCpuLoad = appCpuLoad,
                        memoryHeadroom = memoryHeadroom,
                        lowMemory = memoryInfo.lowMemory,
                        jankRatio = jankRatio,
                        thermalStatus = thermalStatus,
                        thermalHeadroom = thermalHeadroom,
                        batterySaver = powerManager.isPowerSaveMode,
                    )
                smoothedPressure = if (smoothedPressure == 0.0) rawPressure.toDouble() else smoothedPressure * 0.55 + rawPressure * 0.45
                val pressureScore = smoothedPressure.toInt().coerceIn(0, 100)
                val stable =
                    !memoryInfo.lowMemory &&
                        jankRatio < 0.03 &&
                        availableMemoryRatio >= 0.15 &&
                        (systemCpuLoad == null || systemCpuLoad < 0.58) &&
                        appCpuLoad < 0.35 &&
                        thermalStatus < PowerManager.THERMAL_STATUS_MODERATE
                val now = android.os.SystemClock.elapsedRealtime()
                val level = pressureController.sample(pressureScore, stable, now)
                publishRuntimePolicy(level, pressureScore)
                if (stable && availableMemoryRatio >= 0.18) {
                    prefetchStableSamples++
                    if (prefetchStableSamples >= 8) {
                        _prefetchAllowed.value = _runtimePolicy.value.prefetchEnabled
                        prefetchStableSamples = 0
                    }
                } else {
                    prefetchStableSamples = 0
                }
            }
        }
    }

    private data class CpuTimes(
        val total: Long,
        val idle: Long,
    )

    private fun readCpuTimes(): CpuTimes? =
        runCatching {
            val values = File("/proc/stat").useLines { lines ->
                lines.firstOrNull()?.trim()?.split(Regex("\\s+"))
            } ?: return@runCatching null
            if (values.firstOrNull() != "cpu") return@runCatching null
            val ticks = values.drop(1).mapNotNull(String::toLongOrNull)
            if (ticks.size < 5) return@runCatching null
            CpuTimes(total = ticks.sum(), idle = ticks[3] + ticks.getOrElse(4) { 0L })
        }.getOrNull()

    private fun cpuLoad(
        previous: CpuTimes?,
        current: CpuTimes?,
    ): Double? {
        if (previous == null || current == null) return null
        val totalDelta = current.total - previous.total
        val idleDelta = current.idle - previous.idle
        if (totalDelta <= 0L) return null
        return (1.0 - idleDelta.toDouble() / totalDelta).coerceIn(0.0, 1.0)
    }

    private fun normalImageQualityCap(tier: DevicePerformanceTier): AdaptiveImageQuality =
        when (tier) {
            DevicePerformanceTier.LOW -> AdaptiveImageQuality.MID
            DevicePerformanceTier.BALANCED -> AdaptiveImageQuality.HIGH
            DevicePerformanceTier.HIGH -> AdaptiveImageQuality.VERY_HIGH
        }

    private fun publishRuntimePolicy(
        level: RuntimePressureLevel,
        score: Int,
    ) {
        val policy = buildRuntimePolicy(level, score)
        _runtimePolicy.value = policy
        _imageQualityCap.value = policy.imageQualityCap
        if (!policy.prefetchEnabled) _prefetchAllowed.value = false
    }

    private fun buildRuntimePolicy(
        level: RuntimePressureLevel,
        score: Int,
    ): AdaptiveRuntimePolicy {
        val normalQuality = normalImageQualityCap(profile.tier)
        val qualityOrdinal = (normalQuality.ordinal - level.ordinal).coerceAtLeast(AdaptiveImageQuality.VERY_LOW.ordinal)
        val fastInteraction = latestScrollVelocity.get() >= 2_400L
        return AdaptiveRuntimePolicy(
            level = level,
            pressureScore = score.coerceIn(0, 100),
            imageQualityCap = AdaptiveImageQuality.entries[qualityOrdinal],
            imageDecodeParallelism = (profile.imageDecodeParallelism - level.ordinal).coerceAtLeast(1),
            networkRequestParallelism =
                (profile.maxNetworkRequests - level.ordinal).coerceAtLeast(profile.maxNetworkRequestsPerHost),
            prefetchEnabled = level.ordinal < RuntimePressureLevel.VERY_HIGH.ordinal && !(fastInteraction && level.ordinal >= RuntimePressureLevel.HIGH.ordinal),
            animatedMediaEnabled = level.ordinal <= RuntimePressureLevel.ELEVATED.ordinal,
            subtleAnimationsEnabled = profile.animationsEnabled && level.ordinal <= RuntimePressureLevel.ELEVATED.ordinal && !fastInteraction,
            backgroundWorkMultiplier = 1 shl level.ordinal.coerceAtMost(3),
        )
    }

    internal fun calculatePressureScore(
        systemCpuLoad: Double?,
        appCpuLoad: Double,
        memoryHeadroom: Double,
        lowMemory: Boolean,
        jankRatio: Double,
        thermalStatus: Int,
        thermalHeadroom: Float,
        batterySaver: Boolean,
    ): Int {
        if (lowMemory) return 100
        var score = 0
        score += jankPressurePoints(jankRatio)
        score +=
            when {
                memoryHeadroom < 0.02 -> 70
                memoryHeadroom < 0.05 -> 50
                memoryHeadroom < 0.10 -> 30
                memoryHeadroom < 0.15 -> 15
                else -> 0
            }
        score +=
            when {
                thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> 70
                thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE -> 30
                thermalHeadroom >= 1.0f -> 55
                thermalHeadroom >= 0.7f -> 25
                else -> 0
            }
        score +=
            when {
                appCpuLoad >= 0.60 -> 30
                appCpuLoad >= 0.40 -> 20
                appCpuLoad >= 0.25 -> 10
                else -> 0
            }
        score +=
            when {
                systemCpuLoad == null -> 0
                systemCpuLoad >= 0.90 && appCpuLoad < 0.15 -> 3
                systemCpuLoad >= 0.85 && appCpuLoad >= 0.20 -> 10
                systemCpuLoad >= 0.70 && appCpuLoad >= 0.20 -> 5
                else -> 0
            }
        if (appCpuLoad >= 0.40 && jankRatio >= 0.10) score += 20
        if (batterySaver) score += 8
        return score.coerceIn(0, 100)
    }

    internal fun jankPressurePoints(jankRatio: Double): Int =
        when {
            jankRatio >= 0.25 -> 70
            jankRatio >= 0.15 -> 50
            jankRatio >= 0.10 -> 30
            jankRatio >= 0.06 -> 15
            jankRatio >= 0.03 -> 5
            else -> 0
        }

    private fun registerThermalListener(context: Context) {
        if (Build.VERSION.SDK_INT < 29) return
        val powerManager = context.getSystemService(PowerManager::class.java)
        thermalStatus = powerManager.currentThermalStatus
        if (thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) {
            val level = pressureController.requestImmediateElevation(android.os.SystemClock.elapsedRealtime())
            publishRuntimePolicy(level, 90)
            _prefetchAllowed.value = false
        }
        powerManager.addThermalStatusListener { status ->
            thermalStatus = status
            if (status >= PowerManager.THERMAL_STATUS_SEVERE) {
                val level = pressureController.requestImmediateElevation(android.os.SystemClock.elapsedRealtime())
                publishRuntimePolicy(level, 90)
                _prefetchAllowed.value = false
            }
        }
    }

    private fun measure(context: Context): DevicePerformanceMetrics {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        val displayMetrics = context.resources.displayMetrics
        return DevicePerformanceMetrics(
            lowRamDevice = activityManager.isLowRamDevice,
            totalRamMb = (memoryInfo.totalMem / (1024L * 1024L)).toInt(),
            memoryClassMb = activityManager.memoryClass,
            cpuCoreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            cpuMaxFrequencyMhz = readCpuMaxFrequencyMhz(),
            is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            displayMegapixels = displayMetrics.widthPixels.toDouble() * displayMetrics.heightPixels / 1_000_000.0,
        )
    }

    private fun readCpuMaxFrequencyMhz(): Int? {
        val cpuDirectory = File("/sys/devices/system/cpu")
        val frequencies =
            cpuDirectory
                .listFiles()
                .orEmpty()
                .asSequence()
                .filter { it.isDirectory && it.name.matches(Regex("cpu\\d+")) }
                .mapNotNull { cpu ->
                    runCatching {
                        File(cpu, "cpufreq/cpuinfo_max_freq").readText().trim().toLongOrNull()
                    }.getOrNull()
                }.toList()
        return frequencies.maxOrNull()?.div(1_000L)?.toInt()
    }

    private fun currentVersionCode(context: Context): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
    }

}
