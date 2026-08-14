package com.yunfie.illustia.performance

import com.yunfie.illustia.models.Illust

/** Logical tiers are mapped to the discrete variants exposed by Pixiv. */
enum class AdaptiveImageQuality {
    VERY_LOW, // 480 px target / square_medium
    LOW, // 720 px target / medium
    MID, // 1080 px target / large
    HIGH, // 1440 px target / large
    VERY_HIGH, // 2048 px target / large
    ORIGINAL, // Pixiv original
    ;

    val targetPixels: Int?
        get() =
            when (this) {
                VERY_LOW -> 480
                LOW -> 720
                MID -> 1_080
                HIGH -> 1_440
                VERY_HIGH -> 2_048
                ORIGINAL -> null
            }

    fun cappedAt(maximum: AdaptiveImageQuality): AdaptiveImageQuality =
        if (ordinal <= maximum.ordinal) this else maximum
}

fun Illust.imageUrlFor(quality: AdaptiveImageQuality): String =
    when (quality) {
        AdaptiveImageQuality.VERY_LOW -> squareImageUrl.ifBlank { mediumImageUrl.ifBlank { imageUrl } }
        AdaptiveImageQuality.LOW -> mediumImageUrl.ifBlank { imageUrl.ifBlank { squareImageUrl } }
        AdaptiveImageQuality.MID,
        AdaptiveImageQuality.HIGH,
        AdaptiveImageQuality.VERY_HIGH,
        -> imageUrl.ifBlank { mediumImageUrl.ifBlank { squareImageUrl } }
        AdaptiveImageQuality.ORIGINAL -> originalImageUrl?.takeIf(String::isNotBlank) ?: imageUrlFor(AdaptiveImageQuality.HIGH)
    }

fun Illust.imageUrlsFor(quality: AdaptiveImageQuality): List<String> =
    when (quality) {
        AdaptiveImageQuality.VERY_LOW,
        AdaptiveImageQuality.LOW,
        -> mediumImagePages.ifEmpty { listOf(imageUrlFor(quality)) }
        AdaptiveImageQuality.MID,
        AdaptiveImageQuality.HIGH,
        AdaptiveImageQuality.VERY_HIGH,
        -> imagePages.ifEmpty { listOf(imageUrlFor(quality)) }
        AdaptiveImageQuality.ORIGINAL ->
            originalImagePages.ifEmpty {
                imagePages.ifEmpty { listOf(imageUrlFor(quality)) }
            }
    }
