package com.yunfie.illustia.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.yunfie.illustia.performance.AdaptiveGridVelocityController
import com.yunfie.illustia.performance.AdaptiveImageQuality
import com.yunfie.illustia.performance.DevicePerformance
import kotlin.math.abs

private const val ESTIMATED_GRID_ROW_HEIGHT_PX = 720

/** Keeps artwork grids lightweight while still reporting scroll pressure. */
@Composable
fun rememberAdaptiveGridImageQuality(
    state: LazyGridState,
    columnCount: Int,
): AdaptiveImageQuality {
    LaunchedEffect(state, columnCount) {
        var previousPosition = 0L
        var previousTimeNanos = 0L
        val velocityController = AdaptiveGridVelocityController()
        snapshotFlow {
            Triple(state.isScrollInProgress, state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
        }.collect { (scrolling, index, offset) ->
            val now = System.nanoTime()
            val row = index / columnCount.coerceAtLeast(1)
            val position = row.toLong() * ESTIMATED_GRID_ROW_HEIGHT_PX + offset
            if (!scrolling) {
                DevicePerformance.reportScrollVelocity(0.0)
                previousPosition = position
                previousTimeNanos = now
            } else {
                val elapsedSeconds = (now - previousTimeNanos) / 1_000_000_000.0
                val velocity =
                    if (previousTimeNanos == 0L || elapsedSeconds <= 0.0) {
                        0.0
                    } else {
                        abs(position - previousPosition) / elapsedSeconds
                    }
                val decision = velocityController.sample(velocity)
                DevicePerformance.reportScrollVelocity(decision.smoothedVelocity)
                previousPosition = position
                previousTimeNanos = now
            }
        }
    }
    return AdaptiveImageQuality.VERY_LOW
}
