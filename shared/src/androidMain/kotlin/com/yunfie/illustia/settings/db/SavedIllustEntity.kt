package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_illusts")
data class SavedIllustEntity(
    @PrimaryKey
    var illustId: Long = 0L,
    var title: String = "",
    var artistName: String = "",
    var artistId: Long = 0L,
    var thumbUrl: String = "",
    var localCoverPath: String = "",
    var localPagePathsJson: String = "",
    var pageCount: Int = 1,
    var savedAt: Long = 0L,
    var saveGroup: String = "",
    var xRestrict: Int = 0,
)
