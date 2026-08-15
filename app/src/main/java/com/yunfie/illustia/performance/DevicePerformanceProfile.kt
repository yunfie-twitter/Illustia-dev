@file:Suppress("MagicNumber")

package com.yunfie.illustia.performance

enum class DevicePerformanceTier {
    LOW,
    BALANCED,
    HIGH,
}

enum class DevicePerformanceMode(
    val storedValue: String,
) {
    AUTO("auto"),
    LIGHTWEIGHT("lightweight"),
    QUALITY("quality"),
    ;

    companion object {
        fun fromStoredValue(value: String): DevicePerformanceMode = entries.firstOrNull { it.storedValue == value } ?: AUTO
    }
}

/** Raw, independently measurable inputs used by the classifier. */
data class DevicePerformanceMetrics(
    val lowRamDevice: Boolean,
    val totalRamMb: Int,
    val memoryClassMb: Int,
    val cpuCoreCount: Int,
    val cpuMaxFrequencyMhz: Int?,
    val is64Bit: Boolean,
    val sdkInt: Int,
    val displayMegapixels: Double,
    val mediaPerformanceClass: Int = 0,
)

data class DevicePerformanceProfile(
    val tier: DevicePerformanceTier,
    val score: Int,
    val imageDecodeParallelism: Int,
    val imageFetchParallelism: Int,
    val maxNetworkRequests: Int,
    val maxNetworkRequestsPerHost: Int,
    val imageMemoryCachePercent: Double,
    val idlePrefetchLimit: Int,
    val scrollingPrefetchLimit: Int,
    val prefetchDecodeSizePx: Int,
    val smartCacheLimit: Int,
    val animationsEnabled: Boolean,
    val deferredDatabaseLoadDelayMs: Long,
    val postStartupWorkDelayMs: Long,
    val backgroundSyncInitialDelayMs: Long,
    val backgroundSyncPollDelayMs: Long,
    val accountSyncIntervalSeconds: Long,
) {
    val isLowEnd: Boolean get() = tier == DevicePerformanceTier.LOW
}

object DevicePerformanceClassifier {
    @Suppress("CyclomaticComplexMethod")
    fun classify(metrics: DevicePerformanceMetrics): DevicePerformanceProfile {
        var score = 0

        score +=
            when {
                metrics.totalRamMb < 3_072 -> 0
                metrics.totalRamMb < 4_096 -> 1
                metrics.totalRamMb < 6_144 -> 2
                metrics.totalRamMb < 8_192 -> 3
                else -> 4
            }
        score +=
            when {
                metrics.memoryClassMb <= 128 -> 0
                metrics.memoryClassMb <= 192 -> 1
                metrics.memoryClassMb <= 256 -> 2
                metrics.memoryClassMb <= 384 -> 3
                else -> 4
            }
        score +=
            when {
                metrics.cpuCoreCount <= 4 -> 0
                metrics.cpuCoreCount <= 6 -> 1
                metrics.cpuCoreCount <= 8 -> 2
                else -> 3
            }
        score +=
            when (val frequency = metrics.cpuMaxFrequencyMhz) {
                null -> 1

                // Unknown hardware must not be optimistically classified.
                in 0..<1_800 -> 0

                in 1_800..<2_200 -> 1

                in 2_200..<2_800 -> 2

                else -> 3
            }
        score +=
            when {
                metrics.sdkInt <= 28 -> 0
                metrics.sdkInt <= 30 -> 1
                metrics.sdkInt <= 33 -> 2
                else -> 3
            }
        // Media Performance Class is an affirmative capability signal. An undefined
        // value must never be interpreted as evidence that a device is low-end.
        score +=
            when {
                metrics.mediaPerformanceClass >= 35 -> 3
                metrics.mediaPerformanceClass >= 31 -> 2
                metrics.mediaPerformanceClass >= 30 -> 1
                else -> 0
            }
        if (!metrics.is64Bit) score -= 1
        if (metrics.displayMegapixels >= 3.5) score -= 1

        val hardLowEnd =
            metrics.lowRamDevice ||
                metrics.totalRamMb < 3_584 ||
                metrics.memoryClassMb <= 128 ||
                (metrics.cpuCoreCount <= 4 && (metrics.cpuMaxFrequencyMhz ?: 0) < 2_000)
        val tier =
            when {
                hardLowEnd || score <= 7 -> DevicePerformanceTier.LOW
                score <= 12 -> DevicePerformanceTier.BALANCED
                else -> DevicePerformanceTier.HIGH
            }
        return profileFor(tier, score)
    }

    fun applyMode(
        detectedProfile: DevicePerformanceProfile,
        mode: DevicePerformanceMode,
    ): DevicePerformanceProfile =
        when (mode) {
            DevicePerformanceMode.AUTO -> detectedProfile
            DevicePerformanceMode.LIGHTWEIGHT -> profileFor(DevicePerformanceTier.LOW, detectedProfile.score)
            DevicePerformanceMode.QUALITY -> profileFor(DevicePerformanceTier.HIGH, detectedProfile.score)
        }

    @Suppress("LongMethod")
    fun profileFor(
        tier: DevicePerformanceTier,
        score: Int,
    ): DevicePerformanceProfile =
        when (tier) {
            DevicePerformanceTier.LOW -> {
                DevicePerformanceProfile(
                    tier = tier,
                    score = score,
                    imageDecodeParallelism = 1,
                    imageFetchParallelism = 2,
                    maxNetworkRequests = 4,
                    maxNetworkRequestsPerHost = 2,
                    imageMemoryCachePercent = 0.035,
                    idlePrefetchLimit = 4,
                    scrollingPrefetchLimit = 1,
                    prefetchDecodeSizePx = 384,
                    smartCacheLimit = 4,
                    animationsEnabled = false,
                    deferredDatabaseLoadDelayMs = 2_500L,
                    postStartupWorkDelayMs = 4_500L,
                    backgroundSyncInitialDelayMs = 15_000L,
                    backgroundSyncPollDelayMs = 30_000L,
                    accountSyncIntervalSeconds = 60L * 60L,
                )
            }

            DevicePerformanceTier.BALANCED -> {
                DevicePerformanceProfile(
                    tier = tier,
                    score = score,
                    imageDecodeParallelism = 2,
                    imageFetchParallelism = 3,
                    maxNetworkRequests = 6,
                    maxNetworkRequestsPerHost = 3,
                    imageMemoryCachePercent = 0.05,
                    idlePrefetchLimit = 8,
                    scrollingPrefetchLimit = 2,
                    prefetchDecodeSizePx = 448,
                    smartCacheLimit = 8,
                    animationsEnabled = true,
                    deferredDatabaseLoadDelayMs = 750L,
                    postStartupWorkDelayMs = 1_500L,
                    backgroundSyncInitialDelayMs = 5_000L,
                    backgroundSyncPollDelayMs = 15_000L,
                    accountSyncIntervalSeconds = 30L * 60L,
                )
            }

            DevicePerformanceTier.HIGH -> {
                DevicePerformanceProfile(
                    tier = tier,
                    score = score,
                    imageDecodeParallelism = 4,
                    imageFetchParallelism = 6,
                    maxNetworkRequests = 8,
                    maxNetworkRequestsPerHost = 4,
                    imageMemoryCachePercent = 0.07,
                    idlePrefetchLimit = 12,
                    scrollingPrefetchLimit = 4,
                    prefetchDecodeSizePx = 512,
                    smartCacheLimit = 16,
                    animationsEnabled = true,
                    deferredDatabaseLoadDelayMs = 0L,
                    postStartupWorkDelayMs = 300L,
                    backgroundSyncInitialDelayMs = 0L,
                    backgroundSyncPollDelayMs = 5_000L,
                    accountSyncIntervalSeconds = 15L * 60L,
                )
            }
        }
}
