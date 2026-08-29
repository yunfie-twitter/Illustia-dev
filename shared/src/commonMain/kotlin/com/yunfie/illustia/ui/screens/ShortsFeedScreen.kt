package com.yunfie.illustia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfie.illustia.*
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.platform.LocalPlatformActions
import com.yunfie.illustia.ui.components.PixivImage
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Share

@Composable
fun ShortsFeedScreen(
    items: List<Illust>,
    currentIllustId: Long?,
    viewModel: IllustiaViewModel,
    onOpenComments: (Long) -> Unit,
) {
    val platformActions = LocalPlatformActions.current
    val shareLabel = stringResource(R.string.action_share)
    val initialPage =
        remember(items, currentIllustId) {
            items.indexOfFirst { it.id == currentIllustId }.coerceAtLeast(0)
        }
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { items.size },
        )

    LaunchedEffect(pagerState.currentPage, items) {
        items.getOrNull(pagerState.currentPage)?.let {
            viewModel.updateShortsFeedCurrentIllust(it.id)
        }
    }

    LaunchedEffect(pagerState.currentPage, items.size) {
        if (items.isNotEmpty() && pagerState.currentPage >= items.lastIndex - 2) {
            viewModel.loadMoreShortsFeed()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.status_loading),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                val illust = items[page]
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = { viewModel.toggleBookmark(illust) },
                                onDoubleClick = { viewModel.toggleBookmark(illust) },
                            ),
                ) {
                    PixivImage(
                        url = illust.previewUrl,
                        contentDescription = illust.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                                    ),
                                )
                                .padding(start = 16.dp, end = 88.dp, bottom = 28.dp, top = 48.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = illust.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = illust.artistName,
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleBookmark(illust) },
                            modifier = Modifier.size(64.dp),
                            backgroundColor = Color.Black.copy(alpha = 0.56f),
                        ) {
                            Icon(
                                imageVector = if (illust.isBookmarked) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                                contentDescription = stringResource(R.string.action_bookmark),
                                tint = if (illust.isBookmarked) Color(0xFFFF4D67) else Color.White,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                        IconButton(
                            onClick = { onOpenComments(illust.id) },
                            modifier = Modifier.size(64.dp),
                            backgroundColor = Color.Black.copy(alpha = 0.56f),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Messages,
                                contentDescription = stringResource(R.string.detail_comments),
                                tint = Color.White,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                        IconButton(
                            onClick = {
                                platformActions.shareText("https://www.pixiv.net/artworks/${illust.id}", shareLabel)
                            },
                            modifier = Modifier.size(64.dp),
                            backgroundColor = Color.Black.copy(alpha = 0.56f),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Share,
                                contentDescription = shareLabel,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
