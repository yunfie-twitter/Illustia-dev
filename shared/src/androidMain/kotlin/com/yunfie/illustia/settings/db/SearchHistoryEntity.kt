package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    var query: String = "",
    var position: Int = 0,
)
