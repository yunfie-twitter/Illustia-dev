package com.yunfie.illustia.ui.screens

import com.yunfie.illustia.*

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yunfie.illustia.stringResource
import androidx.compose.ui.unit.dp
import com.yunfie.illustia.R
import top.yukonga.miuix.kmp.basic.TabRowWithContour

import org.jetbrains.compose.resources.StringResource

internal enum class HomeTab(
    val labelResId: StringResource,
) {
    Feed(R.string.home_tab_feed),
    Following(R.string.home_tab_following),
}

@Composable
internal fun HomeTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = HomeTab.entries.map { stringResource(it.labelResId) }
    TabRowWithContour(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
