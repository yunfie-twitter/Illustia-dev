package com.yunfie.illustia.models

import androidx.compose.runtime.Immutable
import com.yunfie.illustia.*
import org.jetbrains.compose.resources.StringResource

@Immutable
enum class HomeFeedKind(
    val labelResId: StringResource,
) {
    Recommended(R.string.feed_recommended),
    Ranking(R.string.feed_ranking),
    New(R.string.feed_new),
}

@Immutable
enum class Restrict(
    val apiValue: String,
    val labelResId: StringResource,
) {
    Public("public", R.string.restrict_public),
    Private("private", R.string.restrict_private),
}

@Immutable
data class PageResult<T>(
    val items: List<T>,
    val nextUrl: String?,
)

@Immutable
sealed interface LoadState {
    data object Idle : LoadState

    data object Loading : LoadState

    data object Loaded : LoadState

    data class Error(
        val message: String,
    ) : LoadState
}
