package com.yunfie.illustia

import com.yunfie.illustia.models.Illust
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal fun randomUrlSafeString(byteCount: Int): String {
    val bytes = ByteArray(byteCount)
    SecureRandom().nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

internal fun String.sha256Base64Url(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
}



internal fun interleaveIllusts(
    first: List<Illust>,
    second: List<Illust>,
): List<Illust> =
    buildList(first.size + second.size) {
        val seenIds = HashSet<Long>(first.size + second.size)
        repeat(maxOf(first.size, second.size)) { index ->
            first.getOrNull(index)?.takeIf { seenIds.add(it.id) }?.let(::add)
            second.getOrNull(index)?.takeIf { seenIds.add(it.id) }?.let(::add)
        }
    }
