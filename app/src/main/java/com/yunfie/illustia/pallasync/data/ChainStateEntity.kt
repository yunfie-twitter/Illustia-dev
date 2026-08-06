package com.yunfie.illustia.pallasync.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "pallasync_chain_state")
data class ChainStateEntity(
    @PrimaryKey val chainId: String,
    @ColumnInfo(name = "lamport") val lamport: Long,
    @ColumnInfo(name = "key_epoch") val keyEpoch: Long,
    @ColumnInfo(name = "chain_vector_json") val chainVectorJson: String, // Serialized Map<String, Long>
    @ColumnInfo(name = "latest_control_seq") val latestControlSeq: Long = 0,
    @ColumnInfo(name = "prev_control_hash") val prevControlHash: String = "",
    @ColumnInfo(name = "last_relay_seq", defaultValue = "0") val lastRelaySeq: Long = 0,
    @ColumnInfo(name = "initial_pull_completed", defaultValue = "1") val initialPullCompleted: Boolean = true,
)
