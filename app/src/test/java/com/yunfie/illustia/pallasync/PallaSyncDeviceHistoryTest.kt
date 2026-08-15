package com.yunfie.illustia.pallasync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PallaSyncDeviceHistoryTest :
    StringSpec({
        "item events keep relay recency and delete matching illustrations" {
            val history = PallaSyncDeviceHistory(Json { ignoreUnknownKeys = true })

            history.apply(viewedPayload(1L, "First"))
            history.apply(viewedPayload(2L, "Second"))
            history.apply(viewedPayload(1L, "First updated"))
            history.apply(viewedPayload(2L, "ignored", operation = SYNC_OPERATION_DELETE))

            history.newestFirst().map { it.id to it.title } shouldBe listOf(1L to "First updated")
        }

        "non-viewed and mismatched item events are ignored" {
            val history = PallaSyncDeviceHistory(Json { ignoreUnknownKeys = true })

            history.apply(viewedPayload(1L, "Ignored", entityId = "seen:1"))
            history.apply(viewedPayload(1L, "Ignored", entityId = "viewed:2"))
            history.apply(viewedPayload(1L, "Ignored", operation = "future"))

            history.newestFirst() shouldBe emptyList()
        }

        "legacy snapshots preserve newest-first order and skip malformed entries" {
            val history = PallaSyncDeviceHistory(Json { ignoreUnknownKeys = true })
            val body =
                buildJsonObject {
                    put(
                        "viewedIllusts",
                        buildJsonArray {
                            add(legacyIllust(2L, "Newest"))
                            add(buildJsonObject { put("title", "Missing ID") })
                            add(legacyIllust(1L, "Oldest"))
                        },
                    )
                }

            history.apply(payload(VIEW_HISTORY_SCHEMA_V1, "snapshot", SYNC_OPERATION_UPSERT, body))

            history.newestFirst().map { it.id to it.title } shouldBe
                listOf(2L to "Newest", 1L to "Oldest")
        }
    })

private fun viewedPayload(
    id: Long,
    title: String,
    entityId: String = "viewed:$id",
    operation: String = SYNC_OPERATION_UPSERT,
): DataPayload =
    payload(
        VIEW_HISTORY_SCHEMA_V2,
        entityId,
        operation,
        buildJsonObject {
            put("id", id)
            put("title", title)
            put("artistName", "Artist")
            put("imageUrl", "https://example.com/$id.jpg")
            put("pageCount", 1)
            put("type", "illust")
        },
    )

private fun legacyIllust(
    id: Long,
    title: String,
): JsonElement =
    buildJsonObject {
        put("id", id)
        put("title", title)
        put("artistName", "Artist")
        put("imageUrl", "https://example.com/$id.jpg")
        put("pageCount", 1)
        put("type", "illust")
    }

private fun payload(
    schema: String,
    entityId: String,
    operation: String,
    body: JsonElement,
) = DataPayload(
    schema = schema,
    entity_id = entityId,
    operation = operation,
    context = emptyMap(),
    lamport = 1L,
    created_at_ms = 1L,
    body = body,
)
