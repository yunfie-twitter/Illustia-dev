package com.yunfie.illustia.ui.app

import com.yunfie.illustia.*

import androidx.compose.ui.graphics.vector.ImageVector
import com.yunfie.illustia.R
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.models.UserProfile
import com.yunfie.illustia.models.pixiv.Comment
import com.yunfie.illustia.settings.AppSettings
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.TopDownloads
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.icon.extended.Search as MiuixSearch

import org.jetbrains.compose.resources.StringResource

internal enum class AppTab(
    val id: String,
    val labelResId: StringResource,
    val titleResId: StringResource,
) {
    Home("home", Res.string.nav_home, Res.string.nav_home),
    Novel("novel", Res.string.nav_novel, Res.string.nav_novel),
    Ranking("ranking", Res.string.nav_ranking, Res.string.nav_ranking),
    Bookmarks("bookmarks", Res.string.nav_bookmarks, Res.string.nav_bookmarks_full),
    Search("search", Res.string.nav_search, Res.string.nav_search),
    ShortsFeed("shorts", Res.string.nav_shorts_feed, Res.string.nav_shorts_feed),
    More("more", Res.string.nav_more, Res.string.nav_more);

    val icon: ImageVector
        get() =
            when (this) {
                Home -> MiuixIcons.VerticalSplit
                Novel -> MiuixIcons.Photos
                Ranking -> MiuixIcons.TopDownloads
                Bookmarks -> MiuixIcons.FavoritesFill
                Search -> MiuixIcons.MiuixSearch
                ShortsFeed -> MiuixIcons.Photos
                More -> MiuixIcons.More
            }
}

internal val SwipeTabs = listOf(AppTab.Home, AppTab.Search, AppTab.Bookmarks, AppTab.Ranking, AppTab.More)
internal val VisibleTabs = SwipeTabs

internal fun mainTabs(settings: AppSettings): List<AppTab> {
    val activeTabs =
        listOf(
            AppTab.Home,
            if (settings.shortsFeedEnabled) AppTab.ShortsFeed else AppTab.Search,
            AppTab.Bookmarks,
            AppTab.Ranking,
            AppTab.More,
        )
    val activeById = activeTabs.associateBy(AppTab::id)
    val orderedIds =
        settings.navigationOrder
            .map { id ->
                when {
                    settings.shortsFeedEnabled && id == AppTab.Search.id -> AppTab.ShortsFeed.id
                    !settings.shortsFeedEnabled && id == AppTab.ShortsFeed.id -> AppTab.Search.id
                    else -> id
                }
            }.distinct()
    val preferredTabs = orderedIds.mapNotNull(activeById::get)
    return preferredTabs + activeTabs.filterNot { it in preferredTabs }
}

internal fun visibleTabs(settings: AppSettings): List<AppTab> {
    val tabs = mainTabs(settings)
    val hidden =
        settings.hiddenNavigationTabs.mapTo(mutableSetOf()) { id ->
            when {
                settings.shortsFeedEnabled && id == AppTab.Search.id -> AppTab.ShortsFeed.id
                !settings.shortsFeedEnabled && id == AppTab.ShortsFeed.id -> AppTab.Search.id
                else -> id
            }
        }
    val visible = tabs.filterNot { it.id in hidden }
    return if (visible.size >= 2) visible else tabs.take(2)
}

internal fun startupTabFor(
    value: String,
    tabs: List<AppTab>? = null,
): AppTab {
    val requested =
        when (value) {
            "ranking" -> AppTab.Ranking
            "bookmarks" -> AppTab.Bookmarks
            "search" -> AppTab.Search
            "shorts" -> AppTab.ShortsFeed
            "more" -> AppTab.More
            else -> AppTab.Home
        }
    return requested.takeIf { tabs == null || it in tabs } ?: tabs?.firstOrNull() ?: AppTab.Home
}

internal sealed interface AppRoute {
    data object Main : AppRoute

    data object Search : AppRoute

    data class TagSearch(
        val word: String,
    ) : AppRoute

    data object Onboarding : AppRoute

    data class Detail(
        val illustId: Long,
    ) : AppRoute

    data object ImageViewer : AppRoute

    data object NovelList : AppRoute

    data object NovelReader : AppRoute

    data object Settings : AppRoute

    data object GeneralSettings : AppRoute

    data object ImageSettings : AppRoute

    data object BookmarkSettings : AppRoute

    data object AccountSettings : AppRoute

    data object AccountLoginMethod : AppRoute

    data object DataSettings : AppRoute

    data object ViewHistory : AppRoute

    data object Notifications : AppRoute

    data object MuteSettings : AppRoute

    data object AppData : AppRoute

    data object DownloadQueue : AppRoute

    data object OfflineLibrary : AppRoute

    data object SavedIllustViewer : AppRoute

    data object About : AppRoute

    data object FavoriteTags : AppRoute

    data object WatchlistSeries : AppRoute

    data class UserProfile(
        val userId: Long,
    ) : AppRoute

    data object AppLockSetup : AppRoute

    data object AppLockPinEntry : AppRoute

    data object PrivacyModeSettings : AppRoute

    data object ExperimentalSettings : AppRoute

    data object IllustSeries : AppRoute

    data object PallaSyncSettings : AppRoute

    data object PallaSyncDevices : AppRoute

    data object DevicePairing : AppRoute

    data class DeviceViewHistory(
        val deviceId: String,
        val deviceName: String,
    ) : AppRoute
}

internal data class DetailEntrySnapshot(
    val illust: Illust,
    val relatedIllusts: List<Illust>,
    val firstComment: Comment?,
    val user: UserProfile?,
)
