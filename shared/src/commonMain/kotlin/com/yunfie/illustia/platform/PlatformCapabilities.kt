package com.yunfie.illustia.platform

internal object PlatformCapabilities {
    const val HANDOFF_API = 37

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
    fun supportsVibrationComposition(): Boolean = false
    fun supportsPredefinedVibrationEffect(): Boolean = false
    fun supportsVibrationEffect(): Boolean = false
}
