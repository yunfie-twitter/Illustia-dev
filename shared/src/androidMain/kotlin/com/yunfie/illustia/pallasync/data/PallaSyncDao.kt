package com.yunfie.illustia.pallasync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PallaSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutboxEvent(event: OutboxEntity): Long

    @Query("SELECT * FROM pallasync_outbox WHERE status = 'queued' ORDER BY device_seq ASC")
    suspend fun getQueuedEvents(): List<OutboxEntity>

    @Query("SELECT * FROM pallasync_outbox WHERE chain_id = :chainId AND status = 'queued' ORDER BY device_seq ASC")
    suspend fun getQueuedEvents(chainId: String): List<OutboxEntity>

    @Query("UPDATE pallasync_outbox SET status = 'queued' WHERE chain_id = :chainId AND device_seq = 0 AND status != 'accepted'")
    suspend fun requeueControlEvents(chainId: String)

    @Query(
        """
        UPDATE pallasync_outbox
        SET status = 'queued'
        WHERE chain_id = :chainId AND status = 'pending_initial_merge'
        """,
    )
    suspend fun queuePendingInitialMergeEvents(chainId: String): Int

    @Query(
        """
        UPDATE pallasync_chain_state
        SET initial_pull_completed = 1
        WHERE chainId = :chainId
        """,
    )
    suspend fun markInitialPullCompleted(chainId: String): Int

    @Transaction
    suspend fun completeInitialPullAndQueueEvents(chainId: String): Int {
        check(markInitialPullCompleted(chainId) == 1) {
            "Cannot complete initial pull for inactive chain $chainId"
        }
        return queuePendingInitialMergeEvents(chainId)
    }

    @Query("DELETE FROM pallasync_outbox WHERE status = 'accepted'")
    suspend fun deleteAcceptedEvents()

    @Query("DELETE FROM pallasync_outbox WHERE id = :id")
    suspend fun deleteOutboxEvent(id: Long)

    @Update
    suspend fun updateOutboxEvent(event: OutboxEntity)

    @Query("SELECT * FROM pallasync_chain_state WHERE chainId = :chainId")
    suspend fun getChainState(chainId: String): ChainStateEntity?

    @Query("SELECT * FROM pallasync_chain_state")
    suspend fun getAllChainStates(): List<ChainStateEntity>

    @Query("SELECT * FROM pallasync_chain_state LIMIT 1")
    suspend fun getActiveChainState(): ChainStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChainState(state: ChainStateEntity)

    @Query(
        """
        UPDATE pallasync_chain_state
        SET last_relay_seq = CASE
            WHEN last_relay_seq < :lastRelaySeq THEN :lastRelaySeq
            ELSE last_relay_seq
        END
        WHERE chainId = :chainId
        """,
    )
    suspend fun advanceLastRelaySeq(
        chainId: String,
        lastRelaySeq: Long,
    ): Int

    @Query(
        """
        UPDATE pallasync_chain_state
        SET lamport = CASE WHEN lamport < :lamport THEN :lamport ELSE lamport END
        WHERE chainId = :chainId
        """,
    )
    suspend fun advanceLamport(
        chainId: String,
        lamport: Long,
    ): Int

    @Query("DELETE FROM pallasync_outbox")
    suspend fun clearOutboxEvents()

    @Query("DELETE FROM pallasync_chain_state")
    suspend fun clearChainStates()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: PallaSyncDeviceEntity)

    @Query("SELECT * FROM pallasync_devices WHERE chainId = :chainId")
    suspend fun getDevicesInChain(chainId: String): List<PallaSyncDeviceEntity>

    @Query("SELECT * FROM pallasync_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceSync(deviceId: String): PallaSyncDeviceEntity?

    @Query("SELECT COUNT(*) FROM pallasync_devices WHERE chainId = :chainId")
    suspend fun getDeviceCount(chainId: String): Int

    @Query("DELETE FROM pallasync_devices")
    suspend fun clearDevices()

    @Query("DELETE FROM pallasync_devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Query("SELECT * FROM pallasync_devices")
    suspend fun getAllDevices(): List<PallaSyncDeviceEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM pallasync_inbox
            WHERE chain_id = :chainId AND record_id = :recordId
        )
        """,
    )
    suspend fun hasInboxRecord(
        chainId: String,
        recordId: String,
    ): Boolean

    @Query(
        """
        SELECT * FROM pallasync_inbox
        WHERE chain_id = :chainId AND record_id = :recordId
        LIMIT 1
        """,
    )
    suspend fun getInboxRecord(
        chainId: String,
        recordId: String,
    ): PallaSyncInboxEntity?

    @Query(
        """
        SELECT * FROM pallasync_inbox
        WHERE chain_id = :chainId AND status = 'quarantined'
        ORDER BY relay_seq ASC
        """,
    )
    suspend fun getQuarantinedInboxRecords(chainId: String): List<PallaSyncInboxEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInboxRecords(records: List<PallaSyncInboxEntity>): List<Long>

    @Query("DELETE FROM pallasync_inbox")
    suspend fun clearInboxRecords()

    @Query("DELETE FROM pallasync_inbox WHERE chain_id = :chainId")
    suspend fun clearInboxRecords(chainId: String)

    @Query("DELETE FROM pallasync_outbox WHERE chain_id != :chainId")
    suspend fun deleteOutboxForOtherChains(chainId: String)

    @Query("DELETE FROM pallasync_chain_state WHERE chainId != :chainId")
    suspend fun deleteOtherChainStates(chainId: String)

    @Query("DELETE FROM pallasync_devices WHERE chainId != :chainId")
    suspend fun deleteDevicesForOtherChains(chainId: String)

    @Query("DELETE FROM pallasync_inbox WHERE chain_id != :chainId")
    suspend fun deleteInboxForOtherChains(chainId: String)

    @Transaction
    suspend fun retainOnlyChain(chainId: String) {
        deleteOutboxForOtherChains(chainId)
        deleteOtherChainStates(chainId)
        deleteDevicesForOtherChains(chainId)
        deleteInboxForOtherChains(chainId)
    }

    /** Atomically persists page receipts and advances the durable relay cursor. */
    @Transaction
    suspend fun commitInboxPage(
        chainId: String,
        lastRelaySeq: Long,
        maxLamport: Long,
        records: List<PallaSyncInboxEntity>,
    ) {
        require(lastRelaySeq >= 0) { "lastRelaySeq must not be negative" }
        require(maxLamport >= 0) { "maxLamport must not be negative" }
        require(records.all { it.chainId == chainId && it.relaySeq <= lastRelaySeq }) {
            "Inbox page does not match its chain or cursor"
        }
        insertInboxRecords(records)
        check(advanceLastRelaySeq(chainId, lastRelaySeq) == 1) {
            "Cannot advance cursor for inactive chain $chainId"
        }
        check(advanceLamport(chainId, maxLamport) == 1) {
            "Cannot advance Lamport clock for inactive chain $chainId"
        }
    }

    /** Keeps the Lamport update and the signed outbox insert in one Room transaction. */
    @Transaction
    suspend fun updateChainStateAndInsertOutbox(
        state: ChainStateEntity,
        event: OutboxEntity,
    ): Long {
        require(state.chainId == event.chainId) { "State and outbox chain IDs differ" }
        updateChainState(state)
        return insertOutboxEvent(event)
    }

    @Transaction
    suspend fun updateChainStateAndInsertOutboxEvents(
        state: ChainStateEntity,
        events: List<OutboxEntity>,
    ) {
        require(events.all { it.chainId == state.chainId }) {
            "State and outbox chain IDs differ"
        }
        updateChainState(state)
        events.forEach { insertOutboxEvent(it) }
    }

    /**
     * Switches the single active chain without exposing a partially-cleared local state.
     * Key material is committed separately by PallaSyncKeystore after the remote join
     * succeeds; the coordinator serializes those two commits.
     */
    @Transaction
    suspend fun activateChain(
        state: ChainStateEntity,
        initialEvents: List<OutboxEntity>,
    ) {
        require(initialEvents.all { it.chainId == state.chainId }) {
            "Initial outbox contains a different chain"
        }
        clearOutboxEvents()
        clearChainStates()
        clearDevices()
        clearInboxRecords()
        updateChainState(state)
        initialEvents.forEach { insertOutboxEvent(it) }
    }

    /** Clears all chain-scoped data after the relay explicitly returns HTTP 410. */
    @Transaction
    suspend fun clearActiveChainData() {
        clearOutboxEvents()
        clearChainStates()
        clearDevices()
        clearInboxRecords()
    }
}
