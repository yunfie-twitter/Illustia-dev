package com.yunfie.illustia.settings

import com.yunfie.illustia.pallasync.PallaSyncEventWriter
import com.yunfie.illustia.pallasync.PallaSyncPendingEvent

/** Preserves the outbox-before-settings ordering required for local sync atomicity. */
internal suspend fun <T> persistAfterSyncEnqueue(
    events: List<PallaSyncPendingEvent>,
    writer: PallaSyncEventWriter,
    persist: suspend () -> T,
): T = if (events.isEmpty()) persist() else writer.enqueueDataEventsThen(events, persist)
