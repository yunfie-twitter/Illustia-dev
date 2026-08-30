package com.yunfie.illustia.wallpaper

import android.os.Build

actual object LiveWallpaperSupport {
    actual fun isSupported(): Boolean =
        !isHyperOsDevice(
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            osVersionName = systemProperty("ro.mi.os.version.name"),
            incremental = Build.VERSION.INCREMENTAL,
            display = Build.DISPLAY,
        )
}

private fun systemProperty(name: String): String =
    runCatching {
        val systemProperties = Class.forName("android.os.SystemProperties")
        systemProperties
            .getMethod("get", String::class.java)
            .invoke(null, name) as? String
    }.getOrNull().orEmpty()
