package com.yunfie.illustia.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val ProfileGridHorizontalSpacing = 10.dp
internal val ProfileGridVerticalSpacing = 18.dp

@Composable
internal fun adaptiveProfileGridColumns(): Int {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        profileGridColumnsForWidth(configuration.screenWidthDp)
    }
}

internal fun profileGridColumnsForWidth(screenWidthDp: Int): Int =
    when {
        screenWidthDp >= 1_200 -> 5
        screenWidthDp >= 840 -> 4
        screenWidthDp >= 600 -> 3
        else -> 2
    }

internal fun profileGridContentPadding(
    top: Dp = 14.dp,
    bottom: Dp = 96.dp,
): PaddingValues =
    PaddingValues(
        start = 14.dp,
        end = 14.dp,
        top = top,
        bottom = bottom,
    )
