package com.yunfie.illustia.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.yunfie.illustia.performance.AdaptiveImageQuality
import com.yunfie.illustia.performance.AdaptiveGridVelocityController
import com.yunfie.illustia.performance.DevicePerformance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

private const val ESTIMATED_GRID_ROW_HEIGHT_PX = 720

/** Maps real list velocity to Pixiv's discrete image variants. */
@Composable
fun rememberAdaptiveGridImageQuality(
    state: LazyGridState,
    columnCount: Int,
): AdaptiveImageQuality {
    var quality by remember(state) { mutableStateOf(AdaptiveImageQuality.MID) }
    val runtimeCap by DevicePerformance.imageQualityCap.collectAsState()
    val currentRuntimeCap by rememberUpdatedState(runtimeCap)

    LaunchedEffect(state, columnCount) {
        var previousPosition = 0L
        var previousTimeNanos = 0L
        val velocityController = AdaptiveGridVelocityController()
        snapshotFlow {
            Triple(state.isScrollInProgress, state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
        }.collectLatest { (scrolling, index, offset) ->
            val now = System.nanoTime()
            val row = index / columnCount.coerceAtLeast(1)
            val position = row.toLong() * ESTIMATED_GRID_ROW_HEIGHT_PX + offset
            if (!scrolling) {
                DevicePerformance.reportScrollVelocity(0.0)
                previousPosition = position
                previousTimeNanos = now
                quality = AdaptiveImageQuality.LOW
                delay(300L)
                quality = AdaptiveImageQuality.MID
                delay(600L)
                // After the debounce window the runtime controller alone owns the cap.
                quality = AdaptiveImageQuality.ORIGINAL
                return@collectLatest
            }

            val elapsedSeconds = (now - previousTimeNanos) / 1_000_000_000.0
            val velocity =
                if (previousTimeNanos == 0L || elapsedSeconds <= 0.0) {
                    0.0
                } else {
                    abs(position - previousPosition) / elapsedSeconds
                }
            val decision = velocityController.sample(velocity)
            quality = decision.quality
            DevicePerformance.reportScrollVelocity(decision.smoothedVelocity)
            previousPosition = position
            previousTimeNanos = now
        }
    }
    return quality.cappedAt(currentRuntimeCap)
}
