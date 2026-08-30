package com.yunfie.illustia.platform

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.Test

class PlatformCapabilitiesTest {
    @Test
    fun `API 24 selects every required fallback`() {
        PlatformCapabilities.forSdk(24).apply {
            supportsStorageStats.shouldBeFalse()
            supportsDetailedStorageStats.shouldBeFalse()
            supportsAnimatedImageDecoder.shouldBeFalse()
            supportsClipboardClear.shouldBeFalse()
            supportsScopedMediaStore.shouldBeFalse()
            supportsDynamicColor.shouldBeFalse()
            supportsPlatformLocaleManager.shouldBeFalse()
            supportsRecentsScreenshotControl.shouldBeFalse()
            supportsPredictiveBack.shouldBeFalse()
            supportsRefreshRateHint.shouldBeFalse()
            supportsAdaptiveRefreshRate.shouldBeFalse()
            supportsActivityHandoff.shouldBeFalse()
            supportsWidgetPreview.shouldBeFalse()
            supportsRemoteViewsSharedElement.shouldBeFalse()
            supportsVibratorManager.shouldBeFalse()
            supportsVibrationComposition.shouldBeFalse()
            supportsPredefinedVibrationEffect.shouldBeFalse()
            supportsVibrationEffect.shouldBeFalse()
            requiresLegacyStoragePermission.shouldBeTrue()
        }
    }

    @Test
    fun `capabilities switch at their documented API boundaries`() {
        PlatformCapabilities.forSdk(25).supportsStorageStats.shouldBeFalse()
        PlatformCapabilities.forSdk(26).supportsStorageStats.shouldBeTrue()
        PlatformCapabilities.forSdk(27).supportsClipboardClear.shouldBeFalse()
        PlatformCapabilities.forSdk(28).supportsClipboardClear.shouldBeTrue()
        PlatformCapabilities.forSdk(28).supportsScopedMediaStore.shouldBeFalse()
        PlatformCapabilities.forSdk(29).supportsScopedMediaStore.shouldBeTrue()
        PlatformCapabilities.forSdk(30).supportsVibrationComposition.shouldBeTrue()
        PlatformCapabilities.forSdk(31).supportsDynamicColor.shouldBeTrue()
        PlatformCapabilities.forSdk(33).supportsPlatformLocaleManager.shouldBeTrue()
        PlatformCapabilities.forSdk(35).supportsWidgetPreview.shouldBeTrue()
        PlatformCapabilities.forSdk(36).supportsAdaptiveRefreshRate.shouldBeTrue()
        PlatformCapabilities.forSdk(37).supportsActivityHandoff.shouldBeTrue()
    }
}
