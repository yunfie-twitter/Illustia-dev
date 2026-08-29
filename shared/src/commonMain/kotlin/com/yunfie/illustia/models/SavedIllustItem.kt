package com.yunfie.illustia.models

import kotlinx.serialization.Serializable

@Serializable
data class SavedIllustItem(
    val illustId: Long = 0L,
    val title: String = "",
    val artistName: String = "",
    val artistId: Long = 0L,
    val thumbUrl: String = "",
    val localCoverPath: String = "",
    val localPagePathsJson: String = "",
    val pageCount: Int = 1,
    val savedAt: Long = 0L,
    val saveGroup: String = "",
    val xRestrict: Int = 0,
)
