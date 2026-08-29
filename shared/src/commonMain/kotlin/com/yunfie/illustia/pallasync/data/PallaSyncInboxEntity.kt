package com.yunfie.illustia.pallasync.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Durable receipt for a relay record.
 *
 * A row is written only after a record has either been applied or deliberately
 * quarantined. Together with [ChainStateEntity.lastRelaySeq], this makes relay
 * page processing idempotent across process death and retries.
 */
@Entity(
    tableName = "pallasync_inbox",
    primaryKeys = ["chain_id", "record_id"],
    indices = [
        Index(value = ["chain_id", "relay_seq"]),
        Index(value = ["chain_id", "status"]),
    ],
)
data class PallaSyncInboxEntity(
    @ColumnInfo(name = "chain_id") val chainId: String,
    @ColumnInfo(name = "record_id") val recordId: String,
    @ColumnInfo(name = "relay_seq") val relaySeq: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "raw_record_json") val rawRecordJson: String,
    @ColumnInfo(name = "quarantine_reason") val quarantineReason: String? = null,
    @ColumnInfo(name = "received_at_ms") val receivedAtMs: Long = System.currentTimeMillis(),
) {
    init {
        require(relaySeq >= 0) { "relaySeq must not be negative" }
        require(status == STATUS_APPLIED || status == STATUS_QUARANTINED) {
            "Unsupported inbox status: $status"
        }
        require(status == STATUS_QUARANTINED || quarantineReason == null) {
            "Only quarantined records may have a quarantine reason"
        }
    }

    companion object {
        const val STATUS_APPLIED = "applied"
        const val STATUS_QUARANTINED = "quarantined"

        fun applied(
            chainId: String,
            recordId: String,
            relaySeq: Long,
            rawRecordJson: String,
        ) = PallaSyncInboxEntity(
            chainId = chainId,
            recordId = recordId,
            relaySeq = relaySeq,
            status = STATUS_APPLIED,
            rawRecordJson = rawRecordJson,
        )

        fun quarantined(
            chainId: String,
            recordId: String,
            relaySeq: Long,
            rawRecordJson: String,
            reason: String,
        ) = PallaSyncInboxEntity(
            chainId = chainId,
            recordId = recordId,
            relaySeq = relaySeq,
            status = STATUS_QUARANTINED,
            rawRecordJson = rawRecordJson,
            quarantineReason = reason,
        )
    }
}
