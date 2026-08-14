package com.yunfie.illustia.performance

enum class RuntimePressureLevel {
    NORMAL,
    ELEVATED,
    HIGH,
    VERY_HIGH,
    CRITICAL,
}

data class AdaptiveRuntimePolicy(
    val level: RuntimePressureLevel,
    val pressureScore: Int,
    val imageQualityCap: AdaptiveImageQuality,
    val imageDecodeParallelism: Int,
    val networkRequestParallelism: Int,
    val prefetchEnabled: Boolean,
    val animatedMediaEnabled: Boolean,
    val subtleAnimationsEnabled: Boolean,
    val backgroundWorkMultiplier: Int,
)

class RuntimePressureController {
    var level: RuntimePressureLevel = RuntimePressureLevel.NORMAL
        private set

    private var higherPressureSamples = 0
    private var stableSamples = 0
    private var lockedUntilMs = 0L

    @Synchronized
    fun sample(
        pressureScore: Int,
        stable: Boolean,
        nowMs: Long,
    ): RuntimePressureLevel {
        if (nowMs < lockedUntilMs) {
            higherPressureSamples = 0
            stableSamples = 0
            return level
        }
        val elevationThreshold = intArrayOf(25, 45, 65, 85, 101)[level.ordinal]
        val recoveryThreshold = intArrayOf(-1, 15, 30, 50, 70)[level.ordinal]

        if (pressureScore >= elevationThreshold) {
            higherPressureSamples++
            stableSamples = 0
        } else if (stable && pressureScore <= recoveryThreshold) {
            stableSamples++
            higherPressureSamples = 0
        } else {
            higherPressureSamples = 0
            stableSamples = 0
        }

        if (higherPressureSamples >= 3 && level != RuntimePressureLevel.CRITICAL) {
            level = RuntimePressureLevel.entries[level.ordinal + 1]
            lock(nowMs)
        } else if (stableSamples >= 8 && level != RuntimePressureLevel.NORMAL) {
            level = RuntimePressureLevel.entries[level.ordinal - 1]
            lock(nowMs)
        }
        return level
    }

    @Synchronized
    fun requestImmediateElevation(nowMs: Long): RuntimePressureLevel {
        if (level != RuntimePressureLevel.CRITICAL) {
            level = RuntimePressureLevel.entries[level.ordinal + 1]
            lock(nowMs)
        }
        return level
    }

    private fun lock(nowMs: Long) {
        lockedUntilMs = nowMs + 4_000L
        higherPressureSamples = 0
        stableSamples = 0
    }
}
