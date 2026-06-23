package com.yunfie.illustia.settings.db

import androidx.room.Embedded
import androidx.room.Relation

data class SavedIllustWithPages(
    @Embedded val illust: SavedIllustEntity,
    @Relation(parentColumn = "illustId", entityColumn = "illustId")
    val pages: List<SavedIllustPageEntity>,
)
