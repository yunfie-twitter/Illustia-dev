package com.yunfie.illustia.pallasync

/** Minimal dependency required to atomically enqueue local sync events before persistence. */
internal interface PallaSyncEventWriter {
    suspend fun enqueueDataEvents(events: List<PallaSyncPendingEvent>): Boolean

    suspend fun <T> enqueueDataEventsThen(
        events: List<PallaSyncPendingEvent>,
        afterEnqueue: suspend () -> T,
    ): T
}
