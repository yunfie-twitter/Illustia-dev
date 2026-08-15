package com.yunfie.illustia.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.yunfie.illustia.ui.components.PixivImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

private suspend fun PointerInputScope.detectZoomAndPanGestures(
    isZoomed: () -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var interceptGesture = isZoomed()

        do {
            val event = awaitPointerEvent()
            if (!interceptGesture && event.changes.count { it.pressed } >= 2) {
                interceptGesture = true
            }

            if (interceptGesture && event.changes.none { it.isConsumed }) {
                onGesture(
                    event.calculateCentroid(useCurrent = true),
                    event.calculatePan(),
                    event.calculateZoom(),
                )
                event.changes.forEach { change ->
                    if (change.positionChanged()) change.consume()
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

@Composable
internal fun ZoomablePixivImage(
    url: String,
    highResolutionUrl: String,
    highResolutionRequestSizePx: Int?,
    contentDescription: String,
    isActive: Boolean,
    swipeThresholdPx: Float,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onTap: () -> Unit,
) {
    var scale by remember(url) { mutableFloatStateOf(1f) }
    var offset by remember(url) { mutableStateOf(Offset.Zero) }
    var localScale by remember(url) { mutableFloatStateOf(1f) }
    var localOffset by remember(url) { mutableStateOf(Offset.Zero) }
    var highResolutionRequested by remember(url, highResolutionUrl) { mutableStateOf(false) }
    var highResolutionLoaded by remember(url, highResolutionUrl) { mutableStateOf(false) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val animationScope = rememberCoroutineScope()
    val zoomAnimation = remember { arrayOfNulls<Job>(1) }
    val haptic = LocalHapticFeedback.current

    fun notifyZoomChanged(
        previous: Float,
        current: Float,
    ) {
        val wasZoomed = previous > 1.02f
        val zoomed = current > 1.02f
        if (zoomed) highResolutionRequested = true
        if (wasZoomed != zoomed) onZoomChanged(zoomed)
    }

    fun clampedOffset(
        candidate: Offset,
        atScale: Float,
    ): Offset {
        val maxX = viewportSize.width * (atScale - 1f) / 2f
        val maxY = viewportSize.height * (atScale - 1f) / 2f
        return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
    }

    fun animateTo(
        targetScale: Float,
        targetOffset: Offset,
    ) {
        val startScale = scale
        val startOffset = offset
        zoomAnimation[0]?.cancel()
        zoomAnimation[0] =
            animationScope.launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) { progress, _ ->
                    val previous = scale
                    scale = startScale + (targetScale - startScale) * progress
                    offset =
                        Offset(
                            startOffset.x + (targetOffset.x - startOffset.x) * progress,
                            startOffset.y + (targetOffset.y - startOffset.y) * progress,
                        )
                    localScale = scale
                    localOffset = offset
                    notifyZoomChanged(previous, scale)
                }
            }
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            zoomAnimation[0]?.cancel()
            scale = 1f
            offset = Offset.Zero
            localScale = 1f
            localOffset = Offset.Zero
            onZoomChanged(false)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .onSizeChanged { viewportSize = it }
                .pointerInput(url) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (scale > 1.02f) {
                                animateTo(1f, Offset.Zero)
                            } else {
                                animateTo(2.5f, Offset.Zero)
                            }
                        },
                    )
                }.pointerInput(url) {
                    detectZoomAndPanGestures(
                        isZoomed = { localScale > 1.02f },
                    ) { centroid, pan, zoom ->
                        zoomAnimation[0]?.cancel()
                        val previousScale = localScale
                        val nextScale = (localScale * zoom).coerceIn(1f, 6f)
                        val appliedZoom = nextScale / localScale
                        val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
                        val focalPoint = centroid - viewportCenter
                        val transformedOffset =
                            localOffset + pan +
                                (focalPoint - localOffset) * (1f - appliedZoom)

                        localScale = nextScale
                        localOffset =
                            if (localScale > 1.02f) {
                                clampedOffset(transformedOffset, localScale)
                            } else {
                                Offset.Zero
                            }
                        scale = localScale
                        offset = localOffset
                        notifyZoomChanged(previousScale, localScale)
                    }
                }.then(
                    if (scale > 1.02f) {
                        Modifier.pointerInput(url, swipeThresholdPx) {
                            var accumulatedX = 0f
                            var triggered = false
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    accumulatedX = 0f
                                    triggered = false
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    if (triggered) return@detectHorizontalDragGestures

                                    accumulatedX += dragAmount

                                    if (abs(accumulatedX) >= swipeThresholdPx) {
                                        triggered = true
                                        if (accumulatedX < 0) {
                                            onSwipeNext()
                                        } else {
                                            onSwipePrevious()
                                        }
                                        animateTo(1f, Offset.Zero)
                                        change.consume()
                                    }
                                },
                                onDragEnd = {
                                    accumulatedX = 0f
                                    triggered = false
                                },
                                onDragCancel = {
                                    accumulatedX = 0f
                                    triggered = false
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
    ) {
        val imageModifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin =
                        androidx.compose.ui.graphics
                            .TransformOrigin(0.5f, 0.5f)
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        PixivImage(
            url = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = imageModifier,
            crossfade = false,
        )
        if (highResolutionRequested && highResolutionUrl != url) {
            // Keep the current bitmap visible until the bounded upgrade has decoded successfully.
            PixivImage(
                url = highResolutionUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = imageModifier.alpha(if (highResolutionLoaded) 1f else 0f),
                requestSizePx = highResolutionRequestSizePx,
                onLoadSuccess = { highResolutionLoaded = true },
            )
        }
    }
}
