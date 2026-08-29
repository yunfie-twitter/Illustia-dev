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
    fun setPerformanceMode(newMode: DevicePerformanceMode) {
        mode = newMode
    }
}
