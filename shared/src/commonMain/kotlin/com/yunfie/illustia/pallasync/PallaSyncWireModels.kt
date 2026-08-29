package com.yunfie.illustia.pallasync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal const val PALLASYNC_PROTOCOL_VERSION = "2.0"
internal const val PALLASYNC_PAGE_SIZE = 200
internal const val PALLASYNC_NEXT_SEQ_HEADER = "PallaSync-Next-Seq"
internal const val PALLASYNC_HAS_MORE_HEADER = "PallaSync-Has-More"

@Serializable
internal data class PallaSyncHealth(
    val status: String,
    @SerialName("protocol_version") val protocolVersion: String,
)

@Serializable
internal data class PallaSyncWireRecord(
    @SerialName("protocol_version") val protocolVersion: String,
    @SerialName("chain_id") val chainId: String,
    @SerialName("record_id") val recordId: String,
    @SerialName("collection_name") val collectionName: String,
    val action: String,
    @SerialName("encrypted_payload") val encryptedPayload: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("created_at_ms") val createdAtMs: Long,
    val signature: String,
    @SerialName("relay_seq") val relaySeq: Long? = null,
)

@Serializable
internal data class PallaSyncWireDevice(
    @SerialName("protocol_version") val protocolVersion: String,
    @SerialName("chain_id") val chainId: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("encrypted_device_name") val encryptedDeviceName: String,
    @SerialName("device_public_key") val devicePublicKey: String,
    @SerialName("created_at_ms") val createdAtMs: Long,
    val signature: String,
)

internal data class PallaSyncRecordsPage(
    val records: List<PallaSyncPageRecord>,
    val nextSeq: Long,
    val hasMore: Boolean,
) {
    fun advancesFrom(previousSeq: Long): Boolean =
        nextSeq > previousSeq ||
            (nextSeq == previousSeq && !hasMore && records.isEmpty())
}

internal data class PallaSyncPageRecord(
    val wireRecord: PallaSyncWireRecord?,
    val rawJson: String,
    val parseError: String? = null,
)

internal sealed interface PallaSyncHttpResult<out T> {
    data class Success<T>(
        val value: T,
    ) : PallaSyncHttpResult<T>

    data object Gone : PallaSyncHttpResult<Nothing>

    data class Retryable(
        val message: String,
    ) : PallaSyncHttpResult<Nothing>

    data class ProtocolError(
        val message: String,
        val statusCode: Int? = null,
    ) : PallaSyncHttpResult<Nothing>
}

internal fun classifyPallaSyncHttpStatus(statusCode: Int): PallaSyncHttpResult<Unit>? =
    when {
        statusCode in 200..299 -> {
            null
        }

        statusCode == 410 -> {
            PallaSyncHttpResult.Gone
        }

        statusCode == 429 || statusCode >= 500 -> {
            PallaSyncHttpResult.Retryable(
                "PallaSync server returned HTTP $statusCode",
            )
        }

        else -> {
            PallaSyncHttpResult.ProtocolError(
                "PallaSync server returned HTTP $statusCode",
                statusCode,
            )
        }
    }

internal object PallaSyncUrls {
    fun normalize(rawUrl: String): PallaSyncHttpResult<HttpUrl> {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return PallaSyncHttpResult.ProtocolError("PallaSync server URL is empty")
        }
        if ('?' in trimmed || '#' in trimmed) {
            return PallaSyncHttpResult.ProtocolError("PallaSync server URL must not contain a query or fragment")
        }

        val parsed =
            trimmed.trimEnd('/').toHttpUrlOrNull()
                ?: return PallaSyncHttpResult.ProtocolError("Invalid PallaSync server URL")
        if (parsed.scheme != "https" && parsed.scheme != "http") {
            return PallaSyncHttpResult.ProtocolError("PallaSync server URL must use HTTP or HTTPS")
        }
        if (parsed.username.isNotEmpty() || parsed.password.isNotEmpty()) {
            return PallaSyncHttpResult.ProtocolError("PallaSync server URL must not contain credentials")
        }
        return PallaSyncHttpResult.Success(parsed)
    }

    fun health(baseUrl: HttpUrl): HttpUrl = endpoint(baseUrl, "health")

    fun devices(
        baseUrl: HttpUrl,
        chainId: String,
    ): HttpUrl = endpoint(baseUrl, "chains", chainId, "devices")

    fun records(
        baseUrl: HttpUrl,
        chainId: String,
        afterSeq: Long,
        limit: Int,
    ): HttpUrl =
        recordsEndpoint(baseUrl, chainId)
            .newBuilder()
            .addQueryParameter("after_seq", afterSeq.toString())
            .addQueryParameter("limit", limit.coerceIn(1, 500).toString())
            .build()

    fun recordsEndpoint(
        baseUrl: HttpUrl,
        chainId: String,
    ): HttpUrl = endpoint(baseUrl, "chains", chainId, "records")

    fun chain(
        baseUrl: HttpUrl,
        chainId: String,
    ): HttpUrl = endpoint(baseUrl, "chains", chainId)

    private fun endpoint(
        baseUrl: HttpUrl,
        vararg pathSegments: String,
    ): HttpUrl {
        val builder = baseUrl.newBuilder()
        builder.addPathSegment("pallasync")
        builder.addPathSegment("v2")
        pathSegments.forEach { builder.addPathSegment(it) }
        return builder.build()
    }
}
