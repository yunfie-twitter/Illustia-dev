package com.yunfie.illustia.models

import androidx.compose.runtime.Immutable
import com.yunfie.illustia.*
import org.jetbrains.compose.resources.StringResource

@Immutable
enum class SearchSort(
    val apiValue: String,
    val labelResId: StringResource,
) {
    DateDesc("date_desc", Res.string.sort_date_desc),
    DateAsc("date_asc", Res.string.sort_date_asc),
    PopularDesc("popular_desc", Res.string.sort_popular_desc),
}

@Immutable
enum class SearchTarget(
    val apiValue: String,
    val labelResId: StringResource,
) {
    PartialTags("partial_match_for_tags", Res.string.search_target_tags),
    ExactTags("exact_match_for_tags", Res.string.search_target_exact),
    TitleAndCaption("title_and_caption", Res.string.search_target_title),
}

@Immutable
enum class SearchWorkType(
    val labelResId: StringResource,
) {
    Artworks(Res.string.search_work_type_artworks),
    IllustrationsAndUgoira(Res.string.search_work_type_illustrations_and_ugoira),
    Illustrations(Res.string.search_work_type_illustrations),
    Ugoira(Res.string.search_work_type_ugoira),
    Manga(Res.string.search_work_type_manga),
    Novels(Res.string.search_work_type_novels),
    ;

    val isNovel: Boolean
        get() = this == Novels

    fun acceptsIllustType(type: String): Boolean =
        when (this) {
            Artworks -> type == "illust" || type == "manga" || type == "ugoira"
            IllustrationsAndUgoira -> type == "illust" || type == "ugoira"
            Illustrations -> type == "illust"
            Ugoira -> type == "ugoira"
            Manga -> type == "manga"
            Novels -> false
        }
}

@Immutable
enum class SearchDuration(
    val apiValue: String?,
    val labelResId: StringResource,
) {
    All(null, Res.string.duration_all),
    Day("within_last_day", Res.string.duration_24h),
    Week("within_last_week", Res.string.duration_1week),
    Month("within_last_month", Res.string.duration_1month),
}

@Immutable
enum class SearchBookmarkFilter(
    val keyword: String?,
    val labelResId: StringResource,
) {
    None(null, Res.string.bookmark_filter_none),
    Over100("100users入り", Res.string.bookmark_filter_100),
    Over500("500users入り", Res.string.bookmark_filter_500),
    Over1000("1000users入り", Res.string.bookmark_filter_1000),
    Over5000("5000users入り", Res.string.bookmark_filter_5000),
}
