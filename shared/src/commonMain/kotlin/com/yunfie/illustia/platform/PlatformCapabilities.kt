package com.yunfie.illustia.platform

object PlatformCapabilities {
    const val HANDOFF_API = 37

    fun requiresLegacyStoragePermission(): Boolean = false
    fun supportsStorageStats(): Boolean = false
    fun supportsDetailedStorageStats(): Boolean = false
    fun supportsAnimatedImageDecoder(): Boolean = true
    fun supportsClipboardClear(): Boolean = false
    fun supportsScopedMediaStore(): Boolean = false
    fun supportsDynamicColor(): Boolean = false
    fun supportsPlatformLocaleManager(): Boolean = false
    fun supportsRecentsScreenshotControl(): Boolean = false
    fun supportsPredictiveBack(): Boolean = false
    fun supportsRefreshRateHint(): Boolean = false
    fun supportsAdaptiveRefreshRate(): Boolean = false
    fun supportsActivityHandoff(): Boolean = false
    fun supportsWidgetPreview(): Boolean = false
    fun supportsRemoteViewsSharedElement(): Boolean = false
    fun supportsVibratorManager(): Boolean = false
    fun supportsVibrationComposition(): Boolean = false
    fun supportsPredefinedVibrationEffect(): Boolean = false
    fun supportsVibrationEffect(): Boolean = false

    fun forSdk(sdkInt: Int): PlatformCapabilitySnapshot = PlatformCapabilitySnapshot(sdkInt)
}

class PlatformCapabilitySnapshot(
    sdkInt: Int,
) {
    val supportsStorageStats = sdkInt >= 26
    val supportsDetailedStorageStats = sdkInt >= 35
    val supportsAnimatedImageDecoder = sdkInt >= 28
    val supportsClipboardClear = sdkInt >= 28
    val supportsScopedMediaStore = sdkInt >= 29
    val supportsDynamicColor = sdkInt >= 31
    val supportsPlatformLocaleManager = sdkInt >= 33
    val supportsRecentsScreenshotControl = sdkInt >= 33
    val supportsPredictiveBack = sdkInt >= 33
    val supportsRefreshRateHint = sdkInt >= 30
    val supportsAdaptiveRefreshRate = sdkInt >= 36
    val supportsActivityHandoff = sdkInt >= PlatformCapabilities.HANDOFF_API
    val supportsWidgetPreview = sdkInt >= 35
    val supportsRemoteViewsSharedElement = sdkInt >= 29
    val supportsVibratorManager = sdkInt >= 31
    val supportsVibrationComposition = sdkInt >= 30
    val supportsPredefinedVibrationEffect = sdkInt >= 29
    val supportsVibrationEffect = sdkInt >= 26
    val requiresLegacyStoragePermission = !supportsScopedMediaStore
}

