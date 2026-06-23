package com.yunfie.illustia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yunfie.illustia.IllustiaUiState
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.R
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SavedIllustViewerScreen(state: IllustiaUiState, viewModel: IllustiaViewModel, onBack: () -> Unit) {
    PredictiveBackGestureHandler(onBack = { viewModel.closeSavedIllustViewer(); onBack() })
    val item = state.selectedSavedIllustId?.let { id -> state.savedIllusts.firstOrNull { it.illustId == id } }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = item?.title ?: stringResource(R.string.offline_library_title),
                largeTitle = item?.title ?: stringResource(R.string.offline_library_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = { HeaderIcon(MiuixIcons.Back, onClick = { viewModel.closeSavedIllustViewer(); onBack() }) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).background(MiuixTheme.colorScheme.surface),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(item?.artistName.orEmpty(), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            item {
                AsyncImage(model = item?.localCoverPath, contentDescription = item?.title, modifier = Modifier.fillMaxWidth().height(420.dp))
            }
        }
    }
}
