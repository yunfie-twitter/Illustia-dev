package com.yunfie.illustia.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DevicePerformance {
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
                imageDecodeParallelism = 4,
                networkRequestParallelism = 8,
                prefetchEnabled = true,
                animatedMediaEnabled = true,
                subtleAnimationsEnabled = true,
                backgroundWorkMultiplier = 1,
            ),
        )
    val runtimePolicy: StateFlow<AdaptiveRuntimePolicy> = _runtimePolicy.asStateFlow()

    var profile: DevicePerformanceProfile =
        DevicePerformanceProfile(
            tier = DevicePerformanceTier.HIGH,
            score = 100,
            imageDecodeParallelism = 4,
            imageFetchParallelism = 4,
            maxNetworkRequests = 16,
            maxNetworkRequestsPerHost = 8,
            imageMemoryCachePercent = 0.25,
            idlePrefetchLimit = 10,
            scrollingPrefetchLimit = 4,
            prefetchDecodeSizePx = 512,
            smartCacheLimit = 200,
            animationsEnabled = true,
            deferredDatabaseLoadDelayMs = 0L,
            postStartupWorkDelayMs = 0L,
            backgroundSyncInitialDelayMs = 0L,
            backgroundSyncPollDelayMs = 0L,
            accountSyncIntervalSeconds = 3600L,
        )

    var mode: DevicePerformanceMode = DevicePerformanceMode.AUTO

    fun initialize(context: Any? = null): DevicePerformanceProfile = profile
    fun reportFrame(isJank: Boolean) {}
    fun setAppForeground(foreground: Boolean) {}
    fun onMemoryPressure() {}
    fun recordScrollVelocity(pixelsPerSecond: Float) {}
    fun reportScrollVelocity(velocity: Double) { recordScrollVelocity(velocity.toFloat()) }
    fun recordFrame(durationNanos: Long) {}
    fun recordSlowFrame() {}
    fun calculatePressureScore(
        systemCpuLoad: Double?,
        appCpuLoad: Double,
        memoryHeadroom: Double,
        lowMemory: Boolean,
        jankRatio: Double,
        thermalStatus: Int,
        thermalHeadroom: Float,
        batterySaver: Boolean,
        moderateThermalHeadroomThreshold: Float = 0.7f,
        severeThermalHeadroomThreshold: Float = 1.0f,
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
                thermalStatus >= 3 /* SEVERE */ -> 70
                thermalStatus >= 2 /* MODERATE */ -> 30
                thermalHeadroom >= severeThermalHeadroomThreshold -> 55
                thermalHeadroom >= moderateThermalHeadroomThreshold -> 25
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

    fun jankPressurePoints(jankRatio: Double): Int =
        when {
            jankRatio >= 0.25 -> 70
            jankRatio >= 0.15 -> 50
            jankRatio >= 0.10 -> 30
            jankRatio >= 0.06 -> 15
            jankRatio >= 0.03 -> 5
            else -> 0
        }

    fun setPerformanceMode(newMode: DevicePerformanceMode) {
        mode = newMode
    }
}
