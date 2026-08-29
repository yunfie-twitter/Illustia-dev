package com.yunfie.illustia.nativebridge

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class NativeSavedImage(
    val uri: String,
    val modifiedAtMillis: Long = 0L,
    val size: Long = 0L,
)

class NativeImageStore(
    private val platformContext: Any? = null,
) {
    fun currentPathLabel(): String = "Pictures/Illustia"
    fun folderLabel(folder: String): String = folder.ifBlank { "Pictures/Illustia" }
    fun isDirectPath(): Boolean = true
    fun persistTreeUri(uri: Any): Boolean = true
    fun persistReadOnlyTreeUri(uri: Any): Boolean = true
    fun clearPersistedTreeUri() = Unit

    fun listSavedImages(folder: String? = null): List<NativeSavedImage> {
        val picturesDir = File(System.getProperty("user.home", "."), "Pictures/Illustia")
        if (!picturesDir.exists()) return emptyList()
        return picturesDir.listFiles()?.mapNotNull { file ->
            if (file.isFile) {
                NativeSavedImage(
                    uri = file.absolutePath,
                    modifiedAtMillis = file.lastModified(),
                    size = file.length(),
                )
            } else null
        } ?: emptyList()
    }

    fun save(
        input: InputStream,
        name: String,
        sourceUrl: String,
        responseMimeType: String? = null,
    ): String {
        val picturesDir = File(System.getProperty("user.home", "."), "Pictures/Illustia")
        picturesDir.mkdirs()
        val ext = when {
            responseMimeType?.contains("png") == true || sourceUrl.endsWith(".png") -> ".png"
            responseMimeType?.contains("gif") == true || sourceUrl.endsWith(".gif") -> ".gif"
            else -> ".jpg"
        }
        val target = File(picturesDir, "$name$ext")
        input.use { src ->
            FileOutputStream(target).use { dst ->
                src.copyTo(dst)
            }
        }
        return target.absolutePath
    }
}
