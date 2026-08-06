package com.yunfie.illustia.settings

import com.yunfie.illustia.models.Illust

/**
 * The only settings collections replicated by PallaSync.
 *
 * Keeping this separate from [AppSettings] prevents an incoming sync page from
 * overwriting device-local preferences such as the theme, account, or server URL.
 */
internal data class SyncedCollectionsSnapshot(
    val favoriteTags: List<String> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val mutedTags: List<String> = emptyList(),
    val mutedUsers: List<Long> = emptyList(),
    val mutedIllusts: List<Long> = emptyList(),
    val seenFeedIllusts: List<Long> = emptyList(),
    val viewHistory: List<Illust> = emptyList(),
)

internal fun AppSettings.syncedCollections(): SyncedCollectionsSnapshot {
    return SyncedCollectionsSnapshot(
        favoriteTags = favoriteTags,
        searchHistory = searchHistory,
        mutedTags = mutedTags,
        mutedUsers = mutedUsers,
        mutedIllusts = mutedIllusts,
        seenFeedIllusts = seenFeedIllusts,
        viewHistory = viewHistory,
    )
}

internal fun AppSettings.withSyncedCollections(
    synced: SyncedCollectionsSnapshot,
): AppSettings {
    return copy(
        favoriteTags = synced.favoriteTags,
        searchHistory = synced.searchHistory,
        mutedTags = synced.mutedTags,
        mutedUsers = synced.mutedUsers,
        mutedIllusts = synced.mutedIllusts,
        seenFeedIllusts = synced.seenFeedIllusts,
        viewHistory = synced.viewHistory,
    )
}

/**
 * Replays only the local changes from [base] to [intended] over the latest
 * [persisted] collections. This prevents a queued device-local settings write
 * from undoing records that arrived while it was waiting to be persisted.
 */
internal fun rebaseSyncedCollections(
    base: SyncedCollectionsSnapshot,
    intended: SyncedCollectionsSnapshot,
    persisted: SyncedCollectionsSnapshot,
): SyncedCollectionsSnapshot {
    return SyncedCollectionsSnapshot(
        favoriteTags = rebaseSet(base.favoriteTags, intended.favoriteTags, persisted.favoriteTags),
        searchHistory = rebaseOrdered(base.searchHistory, intended.searchHistory, persisted.searchHistory) { it },
        mutedTags = rebaseSet(base.mutedTags, intended.mutedTags, persisted.mutedTags),
        mutedUsers = rebaseSet(base.mutedUsers, intended.mutedUsers, persisted.mutedUsers),
        mutedIllusts = rebaseSet(base.mutedIllusts, intended.mutedIllusts, persisted.mutedIllusts),
        seenFeedIllusts = rebaseOrdered(
            base.seenFeedIllusts,
            intended.seenFeedIllusts,
            persisted.seenFeedIllusts,
        ) { it },
        viewHistory = rebaseOrdered(base.viewHistory, intended.viewHistory, persisted.viewHistory, Illust::id),
    )
}

private fun <T> rebaseSet(previous: List<T>, next: List<T>, current: List<T>): List<T> {
    val previousSet = previous.toSet()
    val nextDistinct = next.distinct()
    val removed = previousSet - nextDistinct.toSet()
    val result = current.distinct().filterNot { it in removed }.toMutableList()
    nextDistinct.filterNot(previousSet::contains).forEach { item ->
        if (item !in result) result += item
    }
    return result
}

private fun <T, K> rebaseOrdered(
    previous: List<T>,
    next: List<T>,
    current: List<T>,
    key: (T) -> K,
): List<T> {
    val previousDistinct = previous.distinctBy(key)
    val nextDistinct = next.distinctBy(key)
    val currentDistinct = current.distinctBy(key)
    val nextKeys = nextDistinct.mapTo(linkedSetOf(), key)
    val removedKeys = previousDistinct.mapTo(hashSetOf(), key).apply { removeAll(nextKeys) }
    val result = currentDistinct.filterNot { key(it) in removedKeys }.toMutableList()

    orderedMovedPrefix(previousDistinct, nextDistinct, key).asReversed().forEach { item ->
        val itemKey = key(item)
        result.removeAll { key(it) == itemKey }
        result.add(0, item)
    }
    return result
}

private fun <T, K> orderedMovedPrefix(
    previous: List<T>,
    next: List<T>,
    key: (T) -> K,
): List<T> {
    val nextKeys = next.mapTo(linkedSetOf(), key)
    val previousByKey = previous.associateBy(key)
    for (prefixSize in 0..next.size) {
        val movedKeys = next.take(prefixSize).mapTo(hashSetOf(), key)
        val untouchedPrevious = previous.filter { item ->
            val itemKey = key(item)
            itemKey in nextKeys && itemKey !in movedKeys
        }
        val targetSuffix = next.drop(prefixSize)
        if (
            untouchedPrevious.map(key) == targetSuffix.map(key) &&
            targetSuffix.all { item -> previousByKey[key(item)] == item }
        ) {
            return next.take(prefixSize)
        }
    }
    return next
}
