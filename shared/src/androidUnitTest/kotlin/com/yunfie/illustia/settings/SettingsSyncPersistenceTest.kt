package com.yunfie.illustia.settings

import com.yunfie.illustia.pallasync.PallaSyncEventWriter
import com.yunfie.illustia.pallasync.PallaSyncPendingEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonNull
import org.junit.Test

class SettingsSyncPersistenceTest {
    @Test
    fun `enqueue failure prevents settings persistence`() {
        runBlocking {
            var persisted = false
            val writer =
                object : PallaSyncEventWriter {
                    override suspend fun enqueueDataEvents(events: List<PallaSyncPendingEvent>): Boolean = false

                    override suspend fun <T> enqueueDataEventsThen(
                        events: List<PallaSyncPendingEvent>,
                        afterEnqueue: suspend () -> T,
                    ): T = error("outbox unavailable")
                }
            val event = PallaSyncPendingEvent("settings", "search_history", "set", JsonNull)

            shouldThrow<IllegalStateException> {
                persistAfterSyncEnqueue(listOf(event), writer) {
                    persisted = true
                }
            }

            persisted.shouldBeFalse()
        }
    }
}
