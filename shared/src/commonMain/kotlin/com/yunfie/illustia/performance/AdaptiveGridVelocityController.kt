@file:Suppress("MagicNumber")

package com.yunfie.illustia.performance

private const val VERY_LOW_ENTER_VELOCITY = 4_200.0
private const val VERY_LOW_EXIT_VELOCITY = 3_600.0
private const val LOW_ENTER_VELOCITY = 2_200.0
private const val LOW_EXIT_VELOCITY = 1_800.0

data class GridVelocityDecision(
    val smoothedVelocity: Double,
    val quality: AdaptiveImageQuality,
)

/** EWMA plus separate enter/exit thresholds prevents quality oscillation near a boundary. */
class AdaptiveGridVelocityController {
    private var smoothedVelocity = 0.0
    private var quality = AdaptiveImageQuality.MID

    fun sample(rawVelocity: Double): GridVelocityDecision {
        val velocity = rawVelocity.coerceAtLeast(0.0)
        smoothedVelocity =
            if (smoothedVelocity == 0.0) velocity else smoothedVelocity * 0.65 + velocity * 0.35
        quality =
            when (quality) {
                AdaptiveImageQuality.VERY_LOW -> {
                    if (smoothedVelocity < VERY_LOW_EXIT_VELOCITY) {
                        AdaptiveImageQuality.LOW
                    } else {
                        AdaptiveImageQuality.VERY_LOW
                    }
                }

                AdaptiveImageQuality.LOW -> {
                    when {
                        smoothedVelocity >= VERY_LOW_ENTER_VELOCITY -> AdaptiveImageQuality.VERY_LOW
                        smoothedVelocity < LOW_EXIT_VELOCITY -> AdaptiveImageQuality.MID
                        else -> AdaptiveImageQuality.LOW
                    }
                }

                else -> {
                    when {
                        smoothedVelocity >= VERY_LOW_ENTER_VELOCITY -> AdaptiveImageQuality.VERY_LOW
                        smoothedVelocity >= LOW_ENTER_VELOCITY -> AdaptiveImageQuality.LOW
                        else -> AdaptiveImageQuality.MID
                    }
                }
            }
        return GridVelocityDecision(smoothedVelocity, quality)
    }
}
