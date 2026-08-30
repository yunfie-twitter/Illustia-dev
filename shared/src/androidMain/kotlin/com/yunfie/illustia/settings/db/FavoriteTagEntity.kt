package com.yunfie.illustia.settings.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tags")
data class FavoriteTagEntity(
    @PrimaryKey
    var tag: String = "",
    var position: Int = 0,
)
