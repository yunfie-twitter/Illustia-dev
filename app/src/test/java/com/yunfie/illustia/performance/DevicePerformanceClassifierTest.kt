package com.yunfie.illustia.performance

import com.yunfie.illustia.models.Illust
import io.kotest.matchers.shouldBe
import org.junit.Test

class DevicePerformanceClassifierTest {
    @Test
    fun `low ram signal always selects low tier`() {
        val profile =
            DevicePerformanceClassifier.classify(
                metrics(
                    lowRamDevice = true,
                    totalRamMb = 8_192,
                    memoryClassMb = 512,
                    cpuCoreCount = 8,
                    cpuMaxFrequencyMhz = 3_000,
                ),
            )

        profile.tier shouldBe DevicePerformanceTier.LOW
        profile.imageDecodeParallelism shouldBe 1
        profile.animationsEnabled shouldBe false
    }

    @Test
    fun `modern flagship selects high tier`() {
        val profile =
            DevicePerformanceClassifier.classify(
                metrics(
                    totalRamMb = 12_288,
                    memoryClassMb = 512,
                    cpuCoreCount = 8,
                    cpuMaxFrequencyMhz = 3_200,
                    sdkInt = 35,
                ),
            )

        profile.tier shouldBe DevicePerformanceTier.HIGH
        profile.imageDecodeParallelism shouldBe 4
    }

    @Test
    fun `high resolution display prevents optimistic borderline classification`() {
        val normal = DevicePerformanceClassifier.classify(metrics(displayMegapixels = 2.5))
        val dense = DevicePerformanceClassifier.classify(metrics(displayMegapixels = 4.0))

        dense.score shouldBe normal.score - 1
    }

    @Test
    fun `pixiv variants map to discrete adaptive tiers`() {
        val illust =
            Illust(
                id = 1,
                title = "title",
                type = "illust",
                caption = "",
                artistId = 2,
                artistName = "artist",
                artistAvatarUrl = null,
                squareImageUrl = "square",
                mediumImageUrl = "medium",
                imageUrl = "large",
                originalImageUrl = "original",
                tags = emptyList(),
                pageCount = 1,
                isBookmarked = false,
            )

        illust.imageUrlFor(AdaptiveImageQuality.VERY_LOW) shouldBe "square"
        illust.imageUrlFor(AdaptiveImageQuality.LOW) shouldBe "medium"
        illust.imageUrlFor(AdaptiveImageQuality.MID) shouldBe "large"
        illust.imageUrlFor(AdaptiveImageQuality.HIGH) shouldBe "large"
        illust.imageUrlFor(AdaptiveImageQuality.ORIGINAL) shouldBe "original"
    }

    private fun metrics(
        lowRamDevice: Boolean = false,
        totalRamMb: Int = 6_144,
        memoryClassMb: Int = 384,
        cpuCoreCount: Int = 8,
        cpuMaxFrequencyMhz: Int? = 2_600,
        is64Bit: Boolean = true,
        sdkInt: Int = 33,
        displayMegapixels: Double = 2.5,
    ) = DevicePerformanceMetrics(
        lowRamDevice = lowRamDevice,
        totalRamMb = totalRamMb,
        memoryClassMb = memoryClassMb,
        cpuCoreCount = cpuCoreCount,
        cpuMaxFrequencyMhz = cpuMaxFrequencyMhz,
        is64Bit = is64Bit,
        sdkInt = sdkInt,
        displayMegapixels = displayMegapixels,
    )
}
