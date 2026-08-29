package com.yunfie.illustia.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.platform.DesktopPlatformActions
import com.yunfie.illustia.platform.LocalPlatformActions
import com.yunfie.illustia.settings.DesktopSettingsStore
import com.yunfie.illustia.ui.IllustiaApp
import top.yukonga.miuix.kmp.theme.MiuixTheme

fun main() = application {
    val platformActions = remember { DesktopPlatformActions() }
    val settingsStore = remember { DesktopSettingsStore() }
    val viewModel = remember {
        IllustiaViewModel(
            settingsStore = settingsStore,
            platformActions = platformActions,
        )
    }

    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Palleria",
    ) {
        CompositionLocalProvider(
            LocalPlatformActions provides platformActions,
        ) {
            MiuixTheme {
                IllustiaApp(viewModel = viewModel)
            }
        }
    }
}
