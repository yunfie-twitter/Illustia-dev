package com.yunfie.illustia.pallasync

import com.yunfie.illustia.models.Illust
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

/** Rebuilds one device's viewed-illustration history in relay order. */
internal class PallaSyncDeviceHistory(
    private val json: Json,
) {
    private val history = linkedMapOf<Long, Illust>()

    fun apply(payload: DataPayload) {
        when (payload.schema) {
            VIEW_HISTORY_SCHEMA_V1 -> applyLegacySnapshot(payload.body)
            VIEW_HISTORY_SCHEMA_V2 -> applyItemEvent(payload)
        }
    }

    fun newestFirst(): List<Illust> = history.values.toList().asReversed()

    private fun applyLegacySnapshot(body: JsonElement) {
        val viewed = (body as? JsonObject)?.get("viewedIllusts") as? JsonArray ?: return
        viewed.asReversed().forEach { element ->
            element.toLegacyIllust()?.let(::moveToNewest)
        }
    }

    private fun applyItemEvent(payload: DataPayload) {
        val id =
            payload.entity_id
                .takeIf { it.startsWith(VIEWED_ENTITY_PREFIX) }
                ?.removePrefix(VIEWED_ENTITY_PREFIX)
                ?.toLongOrNull()
        if (id != null) {
            when (payload.operation) {
                SYNC_OPERATION_DELETE -> {
                    history.remove(id)
                }

                SYNC_OPERATION_UPSERT -> {
                    runCatching { json.decodeFromJsonElement<ViewedIllustBody>(payload.body) }
                        .getOrNull()
                        ?.takeIf { it.id == id }
                        ?.toIllust()
                        ?.let(::moveToNewest)
                }
            }
        }
    }

    private fun moveToNewest(illust: Illust) {
        history.remove(illust.id)
        history[illust.id] = illust
    }
}

private const val VIEWED_ENTITY_PREFIX = "viewed:"

private fun JsonElement.toLegacyIllust(): Illust? =
    (this as? JsonObject)?.let { item ->
        val id = item["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: return@let null
        val imageUrl = item.string("imageUrl").orEmpty()
        Illust(
            id = id,
            title = item.string("title").orEmpty(),
            type = item.string("type") ?: "illust",
            caption = "",
            artistId = 0,
            artistName = item.string("artistName").orEmpty(),
            artistAvatarUrl = null,
            squareImageUrl = imageUrl,
            mediumImageUrl = imageUrl,
            imageUrl = imageUrl,
            originalImageUrl = null,
            tags = emptyList(),
            pageCount = item["pageCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
            isBookmarked = false,
        )
    }

private fun ViewedIllustBody.toIllust() =
    Illust(
        id = id,
        title = title,
        type = type,
        caption = "",
        artistId = 0,
        artistName = artistName,
        artistAvatarUrl = null,
        squareImageUrl = imageUrl,
        mediumImageUrl = imageUrl,
        imageUrl = imageUrl,
        originalImageUrl = null,
        tags = emptyList(),
        pageCount = pageCount,
        isBookmarked = false,
    )

private fun JsonObject.string(name: String): String? = this[name]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
