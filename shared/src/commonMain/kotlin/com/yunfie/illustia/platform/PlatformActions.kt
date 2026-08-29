package com.yunfie.illustia.platform

import androidx.compose.runtime.staticCompositionLocalOf

interface PlatformActions {
    fun openUrl(url: String)
    fun copyToClipboard(text: String, label: String = "")
    fun shareText(text: String, title: String = "")
    fun showToast(message: String)
    fun restartApp() {}
    fun getPlatformName(): String
    fun getOsVersion(): String
    fun getUserAgent(): String
    fun isDynamicColorSupported(): Boolean = false
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> {
    object : PlatformActions {
        override fun openUrl(url: String) {}
        override fun copyToClipboard(text: String, label: String) {}
        override fun shareText(text: String, title: String) {}
        override fun showToast(message: String) {}
        override fun getPlatformName(): String = "Unknown"
        override fun getOsVersion(): String = "Unknown"
        override fun getUserAgent(): String = "Palleria/5.5.0"
    }
}
