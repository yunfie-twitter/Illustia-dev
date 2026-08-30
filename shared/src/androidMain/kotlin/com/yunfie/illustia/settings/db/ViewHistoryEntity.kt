package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "view_history")
data class ViewHistoryEntity(
    @PrimaryKey
    var id: Long = 0L,
    var title: String = "",
    var artistName: String = "",
    var imageUrl: String = "",
    var pageCount: Int = 1,
    var type: String = "illust",
    var position: Int = 0,
)
