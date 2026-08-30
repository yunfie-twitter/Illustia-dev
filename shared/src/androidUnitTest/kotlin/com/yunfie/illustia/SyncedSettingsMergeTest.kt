package com.yunfie.illustia

import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.settings.AppSettings
import com.yunfie.illustia.settings.SyncedCollectionsSnapshot
import com.yunfie.illustia.settings.rebaseSyncedCollections
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SyncedSettingsMergeTest :
    StringSpec({
        "replaces only synced collections so remote deletions are visible immediately" {
            val local = syncIllust(id = 1L, title = "Local")
            val remote = syncIllust(id = 2L, title = "Remote")
            val current =
                AppSettings(
                    viewHistory = listOf(local),
                    searchHistory = listOf("local query"),
                    favoriteTags = listOf("local tag"),
                    seenFeedIllusts = listOf(1L),
                    mutedTags = listOf("local mute"),
                    themeMode = "dark",
                    pallaSyncServerUrl = "https://device-local.example",
                )
            val synced =
                SyncedCollectionsSnapshot(
                    viewHistory = listOf(remote),
                    searchHistory = emptyList(),
                    favoriteTags = listOf("remote tag"),
                    seenFeedIllusts = listOf(2L),
                )

            val replaced = current.replaceSyncedCollections(synced)

            replaced.viewHistory.map { it.id } shouldBe listOf(2L)
            replaced.searchHistory shouldBe emptyList()
            replaced.favoriteTags shouldBe listOf("remote tag")
            replaced.seenFeedIllusts shouldBe listOf(2L)
            replaced.mutedTags shouldBe emptyList()
            replaced.themeMode shouldBe "dark"
            replaced.pallaSyncServerUrl shouldBe "https://device-local.example"
        }

        "queued local item changes rebase over newer independent remote items" {
            val base =
                SyncedCollectionsSnapshot(
                    favoriteTags = listOf("keep", "remove"),
                    searchHistory = listOf("old"),
                )
            val intended =
                base.copy(
                    favoriteTags = listOf("keep", "local"),
                    searchHistory = listOf("local search", "old"),
                )
            val persisted =
                base.copy(
                    favoriteTags = listOf("remote", "keep", "remove"),
                    searchHistory = listOf("remote search", "old"),
                )

            val rebased = rebaseSyncedCollections(base, intended, persisted)

            rebased.favoriteTags shouldBe listOf("remote", "keep", "local")
            rebased.searchHistory shouldBe listOf("local search", "remote search", "old")
        }
    })

private fun syncIllust(
    id: Long,
    title: String,
): Illust =
    Illust(
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
