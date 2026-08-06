package com.yunfie.illustia.pallasync

import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.settings.SyncedCollectionsSnapshot
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PallaSyncEventApplierTest : StringSpec({
    "item events apply additions removals unmute and history order in one page write" {
        val store = FakeSyncedCollectionsStore(
            SyncedCollectionsSnapshot(
                favoriteTags = listOf("old tag"),
                searchHistory = listOf("old query"),
                mutedTags = listOf("old mute"),
                mutedUsers = listOf(1L),
                mutedIllusts = listOf(2L),
                seenFeedIllusts = listOf(3L),
                viewHistory = listOf(syncIllust(4L, "Old")),
            ),
        )
        val applier = PallaSyncEventApplier(store)
        val events = listOf(
            payload(FAVORITE_TAG_SCHEMA_V2, "old tag", SYNC_OPERATION_DELETE, obj("tag", "old tag")),
            payload(FAVORITE_TAG_SCHEMA_V2, "new tag", SYNC_OPERATION_UPSERT, obj("tag", "new tag")),
            payload(SEARCH_HISTORY_SCHEMA_V2, "old query", SYNC_OPERATION_DELETE, obj("query", "old query")),
            payload(SEARCH_HISTORY_SCHEMA_V2, "first", SYNC_OPERATION_UPSERT, obj("query", "first")),
            payload(SEARCH_HISTORY_SCHEMA_V2, "second", SYNC_OPERATION_UPSERT, obj("query", "second")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "tag:old mute", SYNC_OPERATION_DELETE, muteBody("tag", "old mute")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "tag:new mute", SYNC_OPERATION_UPSERT, muteBody("tag", "new mute")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "user:1", SYNC_OPERATION_DELETE, muteBody("user", "1")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "user:11", SYNC_OPERATION_UPSERT, muteBody("user", "11")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "illust:2", SYNC_OPERATION_DELETE, muteBody("illust", "2")),
            payload(MUTE_SETTINGS_SCHEMA_V2, "illust:12", SYNC_OPERATION_UPSERT, muteBody("illust", "12")),
            payload(VIEW_HISTORY_SCHEMA_V2, "seen:3", SYNC_OPERATION_DELETE, idBody(3L)),
            payload(VIEW_HISTORY_SCHEMA_V2, "seen:13", SYNC_OPERATION_UPSERT, idBody(13L)),
            payload(VIEW_HISTORY_SCHEMA_V2, "viewed:4", SYNC_OPERATION_DELETE, idBody(4L)),
            payload(VIEW_HISTORY_SCHEMA_V2, "viewed:14", SYNC_OPERATION_UPSERT, viewedBody(syncIllust(14L, "New"))),
        )

        applier.applyEvents(events).distinct() shouldBe listOf(PallaSyncApplyResult.Applied)
        store.writes shouldBe 1
        store.value.favoriteTags shouldBe listOf("new tag")
        store.value.searchHistory shouldBe listOf("second", "first")
        store.value.mutedTags shouldBe listOf("new mute")
        store.value.mutedUsers shouldBe listOf(11L)
        store.value.mutedIllusts shouldBe listOf(12L)
        store.value.seenFeedIllusts shouldBe listOf(13L)
        store.value.viewHistory.map { it.id } shouldBe listOf(14L)
    }

    "same entity is relay-order last-writer-wins while different entities merge" {
        val store = FakeSyncedCollectionsStore(SyncedCollectionsSnapshot())
        val applier = PallaSyncEventApplier(store)

        applier.applyEvents(
            listOf(
                payload(FAVORITE_TAG_SCHEMA_V2, "same", SYNC_OPERATION_UPSERT, obj("tag", "same")),
                payload(FAVORITE_TAG_SCHEMA_V2, "other", SYNC_OPERATION_UPSERT, obj("tag", "other")),
                payload(FAVORITE_TAG_SCHEMA_V2, "same", SYNC_OPERATION_DELETE, obj("tag", "same")),
            ),
        )

        store.value.favoriteTags shouldBe listOf("other")
    }

    "legacy snapshots remain union-only and unknown schemas are quarantined" {
        val store = FakeSyncedCollectionsStore(
            SyncedCollectionsSnapshot(favoriteTags = listOf("local")),
        )
        val applier = PallaSyncEventApplier(store)
        val legacyBody = buildJsonArray { add(JsonPrimitive("remote")) }

        val results = applier.applyEvents(
            listOf(
                payload(FAVORITE_TAG_SCHEMA_V1, "snapshot", SYNC_OPERATION_UPSERT, legacyBody),
                payload("palleria.future/9", "future", SYNC_OPERATION_UPSERT, buildJsonObject {}),
            ),
        )

        results.first() shouldBe PallaSyncApplyResult.Applied
        (results.last() is PallaSyncApplyResult.Quarantined) shouldBe true
        store.value.favoriteTags shouldBe listOf("remote", "local")
        store.writes shouldBe 1
    }

    "storage failure leaves the page unapplied and the same record can be retried" {
        val store = FakeSyncedCollectionsStore(SyncedCollectionsSnapshot()).apply {
            failNextUpdate = true
        }
        val applier = PallaSyncEventApplier(store)
        val record = payload(
            FAVORITE_TAG_SCHEMA_V2,
            "retry",
            SYNC_OPERATION_UPSERT,
            obj("tag", "retry"),
        )

        shouldThrow<IllegalStateException> { applier.applyEvents(listOf(record)) }
        store.value.favoriteTags shouldBe emptyList()
        store.writes shouldBe 0

        applier.applyEvents(listOf(record)) shouldBe listOf(PallaSyncApplyResult.Applied)
        store.value.favoriteTags shouldBe listOf("retry")
        store.writes shouldBe 1
    }
})

private class FakeSyncedCollectionsStore(
    initial: SyncedCollectionsSnapshot,
) : SyncedCollectionsStore {
    var value: SyncedCollectionsSnapshot = initial
    var writes: Int = 0
    var failNextUpdate: Boolean = false

    override suspend fun update(
        transform: (SyncedCollectionsSnapshot) -> SyncedCollectionsSnapshot,
    ): SyncedCollectionsSnapshot {
        if (failNextUpdate) {
            failNextUpdate = false
            throw IllegalStateException("simulated storage failure")
        }
        val updated = transform(value)
        if (updated != value) {
            writes += 1
            value = updated
        }
        return value
    }
}

private val eventJson = Json { encodeDefaults = true }

private fun payload(
    schema: String,
    entityId: String,
    operation: String,
    body: JsonElement,
): String {
    return eventJson.encodeToString(
        DataPayload(
            schema = schema,
            entity_id = entityId,
            operation = operation,
            context = emptyMap(),
            lamport = 1L,
            created_at_ms = 1L,
            body = body,
        ),
    )
}

private fun obj(key: String, value: String): JsonElement = buildJsonObject { put(key, value) }

private fun muteBody(kind: String, value: String): JsonElement = buildJsonObject {
    put("kind", kind)
    put("value", value)
}

private fun idBody(id: Long): JsonElement = buildJsonObject { put("id", id) }

private fun viewedBody(illust: Illust): JsonElement = buildJsonObject {
    put("id", illust.id)
    put("title", illust.title)
    put("artistName", illust.artistName)
    put("imageUrl", illust.imageUrl)
    put("pageCount", illust.pageCount)
    put("type", illust.type)
}

private fun syncIllust(id: Long, title: String): Illust {
    return Illust(
        id = id,
        title = title,
        type = "illust",
        caption = "",
        artistId = 0L,
        artistName = "Artist",
        artistAvatarUrl = null,
        squareImageUrl = "image",
        imageUrl = "image",
        originalImageUrl = null,
        tags = emptyList(),
        pageCount = 1,
        isBookmarked = false,
    )
}
