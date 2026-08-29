package com.yunfie.illustia.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yunfie.illustia.stringResource
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.R
import com.yunfie.illustia.data.pixiv.CommentArtworkType
import com.yunfie.illustia.isMutedByTags
import com.yunfie.illustia.ui.components.LoadingIndicator
import com.yunfie.illustia.ui.screens.AboutScreen
import com.yunfie.illustia.ui.screens.AccountLoginMethodScreen
import com.yunfie.illustia.ui.screens.AccountSettingsScreen
import com.yunfie.illustia.ui.screens.AppDataScreen
import com.yunfie.illustia.ui.screens.AppLockSetupScreen
import com.yunfie.illustia.ui.screens.BookmarkSettingsScreen
import com.yunfie.illustia.ui.screens.DataSettingsScreen
import com.yunfie.illustia.ui.screens.DownloadQueueScreen
import com.yunfie.illustia.ui.screens.ExperimentalSettingsScreen
import com.yunfie.illustia.ui.screens.FavoriteTagsScreen
import com.yunfie.illustia.ui.screens.GeneralSettingsScreen
import com.yunfie.illustia.ui.screens.IllustDetailScreen
import com.yunfie.illustia.ui.screens.IllustSeriesScreen
import com.yunfie.illustia.ui.screens.ImageSettingsScreen
import com.yunfie.illustia.ui.screens.ImageViewerScreen
import com.yunfie.illustia.ui.screens.MuteSettingsScreen
import com.yunfie.illustia.ui.screens.NotificationScreen
import com.yunfie.illustia.ui.screens.NovelReaderScreen
import com.yunfie.illustia.ui.screens.NovelScreen
import com.yunfie.illustia.ui.screens.OfflineLibraryScreen
import com.yunfie.illustia.ui.screens.OnboardingScreen
import com.yunfie.illustia.ui.screens.PinSetupScreen
import com.yunfie.illustia.ui.screens.PrivacyModeSettingsScreen
import com.yunfie.illustia.ui.screens.SavedIllustViewerScreen
import com.yunfie.illustia.ui.screens.SearchScreen
import com.yunfie.illustia.ui.screens.SettingsScreen
import com.yunfie.illustia.ui.screens.UserProfileScreen
import com.yunfie.illustia.ui.screens.ViewHistoryScreen
import com.yunfie.illustia.ui.screens.WatchlistSeriesScreen
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AppNavHost(
    appState: IllustiaAppStateBundle,
    viewModel: IllustiaViewModel,
    backStack: List<AppRoute>,
    detailSnapshots: Map<Long, DetailEntrySnapshot>,
    selectedTab: AppTab,
    pagerState: androidx.compose.foundation.pager.PagerState,
    homeScrollBehavior: ScrollBehavior,
    showTokenLogin: Boolean,
    onShowTokenLoginChange: (Boolean) -> Unit,
    selectedWatchlistSeriesId: Long?,
    onSelectedWatchlistSeriesIdChange: (Long?) -> Unit,
    selectedCommentTarget: Pair<Long, CommentArtworkType>?,
    onSelectedCommentTargetChange: (Pair<Long, CommentArtworkType>?) -> Unit,
    onNavigate: (AppRoute) -> Unit,
    onPopRoute: () -> Unit,
    onSearchTag: (String) -> Unit,
    onTabSelected: (Int, AppTab) -> Unit,
) {
    val currentRoute = backStack.lastOrNull() ?: AppRoute.Main

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                (slideOutHorizontally { width -> -width / 4 } + fadeOut())
        },
        modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface),
        label = "AppNavHostContent",
    ) { route ->
        when (route) {
            AppRoute.Main -> {
                MainSurface(
                    appState = appState,
                    viewModel = viewModel,
                    selectedTab = selectedTab,
                    pagerState = pagerState,
                    homeScrollBehavior = homeScrollBehavior,
                    onTabSelected = onTabSelected,
                    onSearch = {
                        if (appState.settings.shortsFeedEnabled) {
                            onNavigate(AppRoute.Search)
                        } else {
                            onTabSelected(mainTabs(appState.settings).indexOf(AppTab.Search), AppTab.Search)
                        }
                    },
                    onOpenNovels = {
                        onNavigate(AppRoute.NovelList)
                    },
                    onOpenComments = { illustId ->
                        onSelectedCommentTargetChange(illustId to CommentArtworkType.ILLUST)
                    },
                    onOpenWatchlistSeries = { seriesId ->
                        onSelectedWatchlistSeriesIdChange(seriesId)
                        onNavigate(AppRoute.IllustSeries)
                    },
                )
            }
            AppRoute.Search -> {
                SearchScreen(state = appState.state, viewModel = viewModel)
            }
            is AppRoute.TagSearch -> {
                SearchScreen(
                    state = appState.state,
                    viewModel = viewModel,
                    onBackFromResults = onPopRoute,
                )
            }
            AppRoute.Onboarding -> {
                var showTokenLoginSheet by remember { mutableStateOf(false) }
                OnboardingScreen(
                    state = appState.state,
                    viewModel = viewModel,
                    onRefreshTokenLogin = { showTokenLoginSheet = true },
                    showTokenLogin = showTokenLoginSheet,
                    onTokenLoginDismiss = { showTokenLoginSheet = false },
                )
            }
            is AppRoute.Detail -> {
                val selectedIllust = appState.state.selectedIllust
                val snapshot =
                    if (selectedIllust?.id == route.illustId) {
                        DetailEntrySnapshot(
                            illust = selectedIllust,
                            relatedIllusts = appState.state.relatedIllusts,
                            firstComment = appState.state.selectedIllustFirstComment,
                            user = appState.state.selectedIllustUser,
                        )
                    } else {
                        detailSnapshots[route.illustId]
                    }
                snapshot?.let { detail ->
                    val illust = detail.illust
                    IllustDetailScreen(
                        illust = illust,
                        relatedIllusts = detail.relatedIllusts,
                        firstComment = detail.firstComment,
                        onBack = onPopRoute,
                        onBookmark = { viewModel.toggleBookmark(illust) },
                        onOpenUser = viewModel::openUser,
                        onOpenComments = {
                            onSelectedCommentTargetChange(illust.id to CommentArtworkType.ILLUST)
                        },
                        onOpenSeries =
                            illust.series?.id?.let { seriesId ->
                                {
                                    onSelectedWatchlistSeriesIdChange(seriesId)
                                    onNavigate(AppRoute.IllustSeries)
                                }
                            },
                        onOpenImage = { page -> viewModel.openImageViewer(illust, page) },
                        onSearchTag = onSearchTag,
                        onLongPressTag = { tag ->
                            viewModel.openTagOptions(
                                rawTag = tag,
                                imageUrl =
                                    illust.squareImageUrl.ifBlank {
                                        illust.thumbnailUrl.ifBlank { illust.imageUrl }
                                    },
                            )
                        },
                        isArtistFollowed = detail.user?.isFollowed == true,
                        isArtistMuted =
                            appState.state.settings.mutedUsers
                                .contains(illust.artistId),
                        isTagMuted = illust.isMutedByTags(appState.state.settings),
                        onToggleFollow = {
                            detail.user?.let { viewModel.toggleFollow(it) }
                                ?: viewModel.openUser(illust.artistId)
                        },
                        onUnmuteUser = { viewModel.unmuteUser(illust.artistId) },
                        onMuteIllust = { viewModel.muteIllust(illust.id) },
                        onMuteUser = { viewModel.muteUser(illust.artistId) },
                        onMuteTag = { tag -> viewModel.muteTag(tag) },
                        onOpenIllust = viewModel::openIllust,
                        onLongPressIllust = viewModel::onIllustLongPress,
                        onOpenIllustById = viewModel::openIllust,
                        onSaveImage = viewModel::saveImage,
                        onSaveAllImages = viewModel::saveImages,
                        onMessage = viewModel::showMessage,
                        loadUgoiraPlayback = viewModel::loadUgoiraPlayback,
                        highQualityImages = appState.state.settings.highQualityImages,
                        detailQuality =
                            if (illust.type == "manga") {
                                appState.state.settings.mangaDetailQuality
                            } else {
                                appState.state.settings.illustDetailQuality
                            },
                        prefetchImages = appState.state.settings.prefetchImages,
                        confirmOnLongPressSave = appState.state.settings.confirmOnLongPressSave,
                        skipConfirmOnDetailSave = appState.state.settings.skipConfirmOnDetailSave,
                        detailSectionOrder = appState.state.settings.detailSectionOrder,
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) { LoadingIndicator() }
            }
            AppRoute.ImageViewer -> {
                appState.state.imageViewerIllust?.let { illust ->
                    ImageViewerScreen(
                        illust = illust,
                        startPage = appState.state.imageViewerStartPage,
                        onBack = onPopRoute,
                        isBookmarked = illust.isBookmarked,
                        onBookmark = { viewModel.toggleBookmark(illust) },
                        onMessage = viewModel::showMessage,
                        fullscreenQuality = appState.state.settings.fullscreenQuality,
                        prefetchImages = appState.state.settings.prefetchImages,
                        mangaReaderMode = appState.state.settings.mangaReaderMode,
                        onPageChanged = viewModel::updateImageViewerPage,
                        loadUgoiraPlayback = viewModel::loadUgoiraPlayback,
                    )
                }
            }
            AppRoute.NovelList -> {
                NovelScreen(
                    items = appState.novelItems,
                    loadState = appState.loadState,
                    nextUrl = appState.novelChrome.novelNextUrl,
                    settings = appState.settings,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                )
            }
            AppRoute.NovelReader -> {
                NovelReaderScreen(
                    novel = appState.state.selectedNovel,
                    text = appState.state.selectedNovelText,
                    loadState = appState.loadState,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                    onRetry = {
                        appState.state.selectedNovel?.let(viewModel::openNovel)
                    },
                )
            }
            AppRoute.Settings -> {
                SettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.GeneralSettings -> {
                GeneralSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.ExperimentalSettings -> {
                ExperimentalSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.ImageSettings -> {
                ImageSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.BookmarkSettings -> {
                BookmarkSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.AccountSettings -> {
                AccountSettingsScreen(
                    state = appState.state,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                    onOpenPallaSync = { onNavigate(AppRoute.PallaSyncSettings) },
                )
            }
            AppRoute.PallaSyncSettings -> {
                com.yunfie.illustia.ui.screens.pallasync.PallaSyncSettingsScreen(
                    state = appState.state,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                    onPairDevice = { onNavigate(AppRoute.DevicePairing) },
                    onDeviceClick = { deviceId, deviceName ->
                        onNavigate(AppRoute.DeviceViewHistory(deviceId, deviceName))
                    },
                )
            }
            AppRoute.PallaSyncDevices -> {
                com.yunfie.illustia.ui.screens.pallasync.PallaSyncDevicesScreen(
                    state = appState.state,
                    onBack = onPopRoute,
                    onDeviceClick = { deviceId, deviceName ->
                        onNavigate(AppRoute.DeviceViewHistory(deviceId, deviceName))
                    },
                )
            }
            AppRoute.DevicePairing -> {
                com.yunfie.illustia.ui.screens.pallasync.DevicePairingScreen(
                    serverUrl = appState.state.settings.pallaSyncServerUrl,
                    onBack = onPopRoute,
                    onPairSuccess = {
                        onPopRoute()
                    },
                )
            }
            AppRoute.AccountLoginMethod -> {
                AccountLoginMethodScreen(
                    onBack = onPopRoute,
                    onWebLogin = viewModel::openWebLogin,
                    onRefreshTokenLogin = { onShowTokenLoginChange(true) },
                )
            }
            AppRoute.DataSettings -> {
                DataSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.ViewHistory -> {
                ViewHistoryScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.Notifications -> {
                NotificationScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.MuteSettings -> {
                MuteSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.AppData -> {
                AppDataScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.DownloadQueue -> {
                DownloadQueueScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.OfflineLibrary -> {
                OfflineLibraryScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.SavedIllustViewer -> {
                SavedIllustViewerScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.About -> {
                AboutScreen(onBack = onPopRoute)
            }
            AppRoute.FavoriteTags -> {
                FavoriteTagsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.WatchlistSeries -> {
                WatchlistSeriesScreen(
                    viewModel = viewModel,
                    onBack = onPopRoute,
                    onOpenSeries = { seriesId ->
                        onSelectedWatchlistSeriesIdChange(seriesId)
                        onNavigate(AppRoute.IllustSeries)
                    },
                )
            }
            AppRoute.IllustSeries -> {
                val currentSeriesId = selectedWatchlistSeriesId
                if (currentSeriesId != null) {
                    IllustSeriesScreen(
                        seriesId = currentSeriesId,
                        viewModel = viewModel,
                        onBack = onPopRoute,
                        onOpenIllust = { illustId -> viewModel.openIllust(illustId) },
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MiuixTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
            is AppRoute.UserProfile -> {
                val selectedUser = appState.state.selectedUser
                if (selectedUser?.id == route.userId) {
                    val user = selectedUser
                    UserProfileScreen(
                        user = user,
                        settings = appState.state.settings,
                        illusts = appState.state.selectedUserIllusts,
                        bookmarks = appState.state.selectedUserBookmarks,
                        relatedUsers = appState.state.selectedRelatedUsers,
                        hasMore = appState.state.selectedUserNextUrl != null,
                        bookmarkHasMore = appState.state.selectedUserBookmarksNextUrl != null,
                        relatedUsersHasMore = appState.state.selectedRelatedUsersNextUrl != null,
                        relatedUsersLoading = appState.state.selectedRelatedUsersLoading,
                        onBack = {
                            if (appState.state.userPageFromSheet) {
                                viewModel.collapseUserPageToSheet()
                            } else {
                                viewModel.hideUserPage()
                                onPopRoute()
                            }
                        },
                        onOpenIllust = { illust ->
                            viewModel.openIllust(illust)
                        },
                        onBookmark = viewModel::toggleBookmark,
                        onLoadMore = viewModel::loadMoreUserIllusts,
                        onLoadBookmarks = viewModel::loadSelectedUserBookmarks,
                        onLoadMoreBookmarks = viewModel::loadMoreSelectedUserBookmarks,
                        onLoadRelatedUsers = viewModel::loadSelectedRelatedUsers,
                        onLoadMoreRelatedUsers = viewModel::loadMoreSelectedRelatedUsers,
                        onOpenRelatedUser = viewModel::openUserPage,
                        onToggleFollow = { viewModel.toggleFollow(user) },
                        onMuteUser = { viewModel.muteUser(user.id) },
                        onMessage = viewModel::showMessage,
                        isMuted =
                            appState.state.settings.mutedUsers
                                .contains(user.id),
                        onUnmuteUser = { viewModel.unmuteUser(user.id) },
                        gridState = viewModel.userProfileGridState(user.id),
                        showHeaderControls = true,
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MiuixTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
            AppRoute.AppLockSetup -> {
                AppLockSetupScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            AppRoute.AppLockPinEntry -> {
                PinSetupScreen(
                    isChange = appState.state.settings.appLockEnabled,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                )
            }
            AppRoute.PrivacyModeSettings -> {
                PrivacyModeSettingsScreen(state = appState.state, viewModel = viewModel, onBack = onPopRoute)
            }
            is AppRoute.DeviceViewHistory -> {
                com.yunfie.illustia.ui.screens.pallasync.DeviceViewHistoryScreen(
                    deviceId = route.deviceId,
                    deviceName = route.deviceName,
                    state = appState.state,
                    viewModel = viewModel,
                    onBack = onPopRoute,
                    onIllustClick = { illust ->
                        viewModel.openIllust(illust)
                        onNavigate(AppRoute.Detail(illust.id))
                    },
                )
            }
        }
    }

    AppOverlayHost(
        appState = appState,
        viewModel = viewModel,
        showTokenLogin = showTokenLogin,
        onDismissTokenLogin = { onShowTokenLoginChange(false) },
        selectedCommentTarget = selectedCommentTarget,
        onDismissComments = { onSelectedCommentTargetChange(null) },
        onSearchTag = onSearchTag,
    )
}
