package com.yunfie.illustia.ui.screens.pallasync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import com.yunfie.illustia.R
import com.yunfie.illustia.IllustiaUiState
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.pallasync.PalleriaSyncManager
import com.yunfie.illustia.ui.components.EmptyState
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.IllustCard
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import com.yunfie.illustia.ui.components.adaptiveIllustColumns
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior

@Composable
fun DeviceViewHistoryScreen(
    deviceId: String,
    deviceName: String,
    state: IllustiaUiState,
    viewModel: IllustiaViewModel,
    onBack: () -> Unit,
    onIllustClick: (Illust) -> Unit,
) {
    PredictiveBackGestureHandler(onBack = onBack)
    val context = LocalContext.current
    val syncManager = remember { PalleriaSyncManager(context = context) }
    
    var viewHistory by remember { mutableStateOf<List<Illust>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val gridState = rememberLazyGridState()
    val feedHighQuality = state.settings.useHighQualityFeedImages
    val showAiBadge = state.settings.showAiBadge

    LaunchedEffect(deviceId) {
        isLoading = true
        viewHistory = syncManager.getDeviceViewHistory(deviceId)
        isLoading = false
    }

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = deviceName,
                largeTitle = deviceName,
                subtitle = "デバイスの閲覧履歴",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    HeaderIcon(MiuixIcons.Back, onClick = onBack)
                },
            )
        },
    ) { scaffoldPadding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(adaptiveIllustColumns(state.settings)),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MiuixTheme.colorScheme.surface),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = scaffoldPadding.calculateTopPadding() + 14.dp,
                bottom = 96.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            
            if (isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp).fillMaxWidth())
                }
            } else if (viewHistory.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(stringResource(R.string.search_empty_illust))
                }
            } else {
                val historyList = viewHistory ?: emptyList()
                gridItems(historyList, key = { it.id }, contentType = { "illust_card" }) { illust ->
                    LaunchedEffect(illust.id) {
                        if (illust.artistId == 0L) {
                            viewModel.lazyLoadPartialIllust(illust.id)
                        }
                    }
                    IllustCard(
                        illust = illust,
                        isSelected = false,
                        onBookmark = { viewModel.toggleBookmark(illust) },
                        onClick = { onIllustClick(illust) },
                        onLongClick = { },
                        highQualityImages = feedHighQuality,
                        showAiBadge = showAiBadge,
                    )
                }
            }
        }
    }
}
