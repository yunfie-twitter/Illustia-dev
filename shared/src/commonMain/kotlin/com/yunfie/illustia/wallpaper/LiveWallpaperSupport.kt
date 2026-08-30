package com.yunfie.illustia.wallpaper

import com.yunfie.illustia.nativebridge.NativeSavedImage
import com.yunfie.illustia.settings.AppSettings

expect object LiveWallpaperSupport {
    fun isSupported(): Boolean
}

fun selectCandidate(
    candidates: List<NativeSavedImage>,
    settings: AppSettings,
    currentPath: String?,
    forceDifferent: Boolean,
): NativeSavedImage? {
    if (candidates.isEmpty()) return null
    val ordered =
        when (settings.liveWallpaperOrder) {
            "newest" -> candidates.sortedByDescending { it.modifiedAtMillis }
            "oldest" -> candidates.sortedBy { it.modifiedAtMillis }
            else -> candidates.shuffled()
        }
    if (!forceDifferent || ordered.size == 1) return ordered.first()
    val currentIndex = ordered.indexOfFirst { it.uri == currentPath }
    return when {
        settings.liveWallpaperOrder == "random" -> ordered.firstOrNull { it.uri != currentPath }
        currentIndex < 0 -> ordered.first()
        else -> ordered[(currentIndex + 1) % ordered.size]
    }
}

fun isHyperOsDevice(
    manufacturer: String,
    brand: String,
    osVersionName: String,
    incremental: String,
    display: String,
): Boolean {
    val isXiaomiFamily =
        manufacturer.equals("Xiaomi", ignoreCase = true) ||
            brand.equals("Xiaomi", ignoreCase = true) ||
            brand.equals("Redmi", ignoreCase = true) ||
            brand.equals("POCO", ignoreCase = true) ||
            brand.equals("Blackshark", ignoreCase = true)
    if (!isXiaomiFamily) return false

    return osVersionName.contains("OS", ignoreCase = true) ||
        incremental.startsWith("OS", ignoreCase = true) ||
        display.contains("HyperOS", ignoreCase = true)
}

