package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    var userId: Long = 0L,
    var name: String = "",
    var account: String = "",
    var profileImageUrl: String? = null,
    var position: Int = 0,
)
