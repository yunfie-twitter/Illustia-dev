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
    private val ROUTE_TRAILING_PUNCTUATION =
        charArrayOf(
            '.',
            ',',
            ';',
            ':',
            '!',
            '?',
            ')',
            ']',
            '}',
            '。',
            '、',
            '！',
            '？',
            '）',
            '】',
            '』',
            '」',
        )

    const val EXTRA_HANDOFF_URI = "com.yunfie.illustia.EXTRA_HANDOFF_URI"
    const val MAX_PROCESS_TEXT_CODE_POINTS = 256

    fun parseText(value: String?): NativeIntentEvent? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isEmpty()) return null
        parseUri(normalized)?.let { return it }
        return ROUTE_CANDIDATE_PATTERN
            .findAll(normalized)
            .mapNotNull { match -> parseUri(match.value.trimEnd(*ROUTE_TRAILING_PUNCTUATION)) }
            .firstOrNull()
    }

    fun normalizeProcessText(value: CharSequence?): String? {
        if (value == null) return null
        val normalized =
            buildString(value.length.coerceAtMost(MAX_PROCESS_TEXT_CODE_POINTS)) {
                var pendingSpace = false
                value.forEach { char ->
                    when {
                        char.isWhitespace() -> {
                            pendingSpace = isNotEmpty()
                        }

                        char.isISOControl() -> {
                            Unit
                        }

                        else -> {
                            if (pendingSpace) append(' ')
                            append(char)
                            pendingSpace = false
                        }
                    }
                }
            }.trim()
                .removePrefix("#")
                .trimStart()
        if (normalized.isBlank()) return null

        val codePointCount = normalized.codePointCount(0, normalized.length)
        if (codePointCount <= MAX_PROCESS_TEXT_CODE_POINTS) return normalized
        val endIndex = normalized.offsetByCodePoints(0, MAX_PROCESS_TEXT_CODE_POINTS)
        return normalized.substring(0, endIndex).trimEnd()
    }

    private fun parseUri(value: String?): NativeIntentEvent? {
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        val normalizedScheme = uri.scheme?.lowercase() ?: return null
        val normalizedHost = uri.host?.lowercase() ?: return null
        if (!isTrustedPixivRoute(normalizedScheme, normalizedHost)) return null
        val segments =
            uri.rawPath
                .orEmpty()
                .split('/')
                .filter(String::isNotEmpty)
        val artworkIndex = segments.indexOfFirst { it == "artworks" || it == "illusts" }
        if (artworkIndex >= 0) {
            segments.getOrNull(artworkIndex + 1)?.toLongOrNull()?.let {
                return NativeIntentEvent.Artwork(it)
            }
        }
        if (normalizedHost == "illusts") {
            segments.firstOrNull()?.toLongOrNull()?.let { return NativeIntentEvent.Artwork(it) }
        }
        val userIndex = segments.indexOfFirst { it == "users" }
        if (userIndex >= 0) {
            segments.getOrNull(userIndex + 1)?.toLongOrNull()?.let {
                return NativeIntentEvent.User(it)
            }
        }
        if (normalizedHost == "users") {
            segments.firstOrNull()?.toLongOrNull()?.let { return NativeIntentEvent.User(it) }
        }
        return null
    }

    private fun isTrustedPixivRoute(
        normalizedScheme: String,
        normalizedHost: String,
    ): Boolean =
        when (normalizedScheme) {
            "http", "https" -> normalizedHost in WEB_PIXIV_HOSTS
            in CUSTOM_PIXIV_SCHEMES -> normalizedHost in CUSTOM_PIXIV_HOSTS
            else -> false
        }
}

