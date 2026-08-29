package com.yunfie.illustia.nativebridge

import java.net.URI

sealed interface NativeIntentEvent {
    data class Artwork(val id: Long) : NativeIntentEvent
    data class User(val id: Long) : NativeIntentEvent
    data class Text(val value: String) : NativeIntentEvent
    data class Image(val uri: String) : NativeIntentEvent
}

object NativeIntentRouter {
    private val WEB_PIXIV_HOSTS = setOf("pixiv.net", "www.pixiv.net")
    private val CUSTOM_PIXIV_SCHEMES = setOf("pixiv", "pixez")
    private val CUSTOM_PIXIV_HOSTS = setOf("pixiv.net", "www.pixiv.net", "users", "illusts")
    private val ROUTE_CANDIDATE_PATTERN = Regex("""(?i)\b(?:https?://|pixiv://|pixez://)\S+""")

    fun parseText(text: String?): NativeIntentEvent? {
        if (text.isNullOrBlank()) return null
        val trimmed = text.trim()
        val candidate = ROUTE_CANDIDATE_PATTERN.find(trimmed)?.value ?: trimmed
        return runCatching {
            val uri = URI.create(candidate)
            when (uri.scheme?.lowercase()) {
                "http", "https" -> {
                    val host = uri.host?.lowercase() ?: return@runCatching null
                    if (host !in WEB_PIXIV_HOSTS) return@runCatching null
                    val path = uri.path.orEmpty()
                    when {
                        path.startsWith("/artworks/") -> {
                            path.removePrefix("/artworks/").takeWhile { it.isDigit() }.toLongOrNull()?.let(NativeIntentEvent::Artwork)
                        }
                        path.startsWith("/users/") -> {
                            path.removePrefix("/users/").takeWhile { it.isDigit() }.toLongOrNull()?.let(NativeIntentEvent::User)
                        }
                        else -> null
                    }
                }
                in CUSTOM_PIXIV_SCHEMES -> {
                    val host = uri.host?.lowercase() ?: return@runCatching null
                    val path = uri.path.orEmpty().trim('/')
                    when {
                        host == "illusts" || host == "artworks" || path.startsWith("artworks/") -> {
                            (path.takeWhile { it.isDigit() }.ifEmpty { uri.path.orEmpty().removePrefix("/artworks/").takeWhile { it.isDigit() } })
                                .toLongOrNull()?.let(NativeIntentEvent::Artwork)
                        }
                        host == "users" || path.startsWith("users/") -> {
                            (path.takeWhile { it.isDigit() }.ifEmpty { uri.path.orEmpty().removePrefix("/users/").takeWhile { it.isDigit() } })
                                .toLongOrNull()?.let(NativeIntentEvent::User)
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }.getOrNull() ?: NativeIntentEvent.Text(trimmed)
    }
}
