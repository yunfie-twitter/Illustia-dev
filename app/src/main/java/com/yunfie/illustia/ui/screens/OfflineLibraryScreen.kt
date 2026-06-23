package com.yunfie.illustia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yunfie.illustia.IllustiaUiState
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.R
import com.yunfie.illustia.ui.components.ElevatedPanel
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OfflineLibraryScreen(state: IllustiaUiState, viewModel: IllustiaViewModel, onBack: () -> Unit) {
    PredictiveBackGestureHandler(onBack = onBack)
    LaunchedEffect(Unit) { viewModel.loadSavedLibrary() }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.offline_library_title),
                largeTitle = stringResource(R.string.offline_library_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = { HeaderIcon(MiuixIcons.Back, onClick = onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection).background(MiuixTheme.colorScheme.surface),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.savedIllusts.isEmpty()) {
                item { Text(stringResource(R.string.offline_library_empty), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            } else {
                items(state.savedIllusts, key = { it.illustId }) { item ->
                    ElevatedPanel(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = item.localCoverPath.ifBlank { item.thumbUrl },
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, MiuixTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(item.title.orEmpty(), color = MiuixTheme.colorScheme.onBackground)
                                Text(item.artistName.orEmpty(), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Button(onClick = { viewModel.openSavedIllustViewer(item.illustId) }) {
                                    Text(stringResource(R.string.action_open))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
