package com.yunfie.illustia.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowRgb565
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import com.yunfie.illustia.data.proxyPixivImageUrl
import com.yunfie.illustia.performance.DevicePerformance

val PixivImageHeaders =
    NetworkHeaders
        .Builder()
        .set("Referer", "https://www.pixiv.net/")
        .set("User-Agent", "PixivAndroidApp/6.184.0 (Android 14; Illustia)")
        .build()

private const val ThumbnailDecodeSizePx = 512

@Composable
fun PixivImage(
    url: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    crossfade: Boolean = false,
    thumbnail: Boolean = false,
    requestSizePx: Int? = null,
    onLoadSuccess: (() -> Unit)? = null,
    onSuccess: ((Bitmap) -> Unit)? = null,
) {
    val context = LocalPlatformContext.current
    val runtimePolicy by DevicePerformance.runtimePolicy.collectAsState()
    val proxyBaseUrl = LocalPixivImageProxyBaseUrl.current
    val effectiveUrl =
        remember(url, proxyBaseUrl) {
            proxyPixivImageUrl(url, proxyBaseUrl)
        }
    val currentOnSuccess by rememberUpdatedState(onSuccess)
    val currentOnLoadSuccess by rememberUpdatedState(onLoadSuccess)
    val imageRequest =
        remember(effectiveUrl, contentScale, thumbnail, requestSizePx, runtimePolicy.subtleAnimationsEnabled) {
            ImageRequest
                .Builder(context)
                .data(effectiveUrl)
                .httpHeaders(PixivImageHeaders)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .crossfade(!thumbnail && crossfade && runtimePolicy.subtleAnimationsEnabled)
                .listener(
                    onSuccess = { _, result ->
                        currentOnLoadSuccess?.invoke()
                        runCatching {
                            currentOnSuccess?.invoke(result.image.toBitmap())
                        }
                    },
                ).apply {
                    if (thumbnail || requestSizePx != null) {
                        size(requestSizePx ?: ThumbnailDecodeSizePx)
                        scale(if (contentScale == ContentScale.Crop) Scale.FILL else Scale.FIT)
                        precision(Precision.INEXACT)
                        allowRgb565(thumbnail)
                    }
                }.build()
        }
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
fun PrefetchPixivImages(
    urls: List<String>,
    enabled: Boolean,
    limit: Int = 12,
    isScrolling: Boolean = false,
) {
    val context = LocalPlatformContext.current
    val proxyBaseUrl = LocalPixivImageProxyBaseUrl.current
    val performance = DevicePerformance.profile
    val prefetchAllowed by DevicePerformance.prefetchAllowed.collectAsState()
    val effectiveLimit =
        minOf(
            limit,
            if (isScrolling) performance.scrollingPrefetchLimit else performance.idlePrefetchLimit,
        )
    val prefetchUrls =
        remember(urls, proxyBaseUrl, effectiveLimit) {
            urls
                .asSequence()
                .filter { it.isNotBlank() }
                .map { proxyPixivImageUrl(it, proxyBaseUrl) }
                .distinct()
                .take(effectiveLimit)
                .toList()
        }

    val activeRequests = remember { mutableMapOf<String, () -> Unit>() }

    LaunchedEffect(enabled, prefetchAllowed, prefetchUrls) {
        if (!enabled || !prefetchAllowed || prefetchUrls.isEmpty()) {
            activeRequests.values.forEach { cancel -> cancel() }
            activeRequests.clear()
            return@LaunchedEffect
        }

        val newUrls = prefetchUrls.toSet()
        activeRequests.keys
            .filterNotTo(mutableListOf()) { it in newUrls }
            .forEach { url -> activeRequests.remove(url)?.invoke() }

        val imageLoader = SingletonImageLoader.get(context)
        newUrls.forEach { url ->
            if (url !in activeRequests) {
                val request =
                    ImageRequest
                        .Builder(context)
                        .data(url)
                        .httpHeaders(PixivImageHeaders)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .size(performance.prefetchDecodeSizePx)
                        .scale(Scale.FILL)
                        .precision(Precision.INEXACT)
                        .allowRgb565(true)
                        .build()
                val disposable = imageLoader.enqueue(request)
                activeRequests[url] = disposable::dispose
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRequests.values.forEach { cancel -> cancel() }
            activeRequests.clear()
        }
    }
}
