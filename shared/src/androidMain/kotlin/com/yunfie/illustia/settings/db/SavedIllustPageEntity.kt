package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_illust_pages")
data class SavedIllustPageEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var illustId: Long = 0L,
    var pageIndex: Int = 0,
    var localPath: String = "",
    var sourceUrl: String = "",
)
