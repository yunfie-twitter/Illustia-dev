package com.yunfie.illustia.data

data class AppStorageUsage(
    val appBytes: Long = 0L,
    val dataBytes: Long = 0L,
    val cacheBytes: Long = 0L,
    val apkBytes: Long? = null,
    val optimizedCodeBytes: Long? = null,
    val dexMetadataBytes: Long? = null,
    val nativeLibraryBytes: Long? = null,
    val referenceProfileBytes: Long? = null,
    val currentProfileBytes: Long? = null,
) {
    val totalBytes: Long
        get() = appBytes + dataBytes

    val userDataBytes: Long
        get() = (dataBytes - cacheBytes).coerceAtLeast(0L)

    val hasCodeBreakdown: Boolean
        get() = apkBytes != null
}

fun readAppStorageUsage(): AppStorageUsage =
    AppStorageUsage(
        appBytes = 35L * 1024 * 1024,
        dataBytes = 15L * 1024 * 1024,
        cacheBytes = 5L * 1024 * 1024,
    )
