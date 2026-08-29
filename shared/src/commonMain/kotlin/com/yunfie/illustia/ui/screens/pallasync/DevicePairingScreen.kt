package com.yunfie.illustia.ui.screens.pallasync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yunfie.illustia.*
import com.yunfie.illustia.platform.LocalPlatformActions
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DevicePairingScreen(
    serverUrl: String,
    onBack: () -> Unit,
    onPairSuccess: () -> Unit,
) {
    PredictiveBackGestureHandler(onBack = onBack)
    val platformActions = LocalPlatformActions.current
    val scope = rememberCoroutineScope()

    var seedPhrase by remember { mutableStateOf("") }
    var enteredSeedPhrase by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }

    val tabs =
        listOf(
            stringResource(R.string.pallasync_view_sync_code),
            stringResource(R.string.pallasync_enter_sync_code),
        )
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.pallasync_sync_devices),
                navigationIcon = {
                    HeaderIcon(MiuixIcons.Back, onClick = onBack)
                },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.surface)
                    .padding(scaffoldPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TabRowWithContour(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (selectedTabIndex == 0) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.pallasync_recovery_phrase_desc),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (seedPhrase.isNotEmpty()) {
                        val words = seedPhrase.trim().split("\\s+".toRegex())
                        if (words.size == 24) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                itemsIndexed(words) { index, word ->
                                    Row(
                                        modifier =
                                            Modifier
                                                .background(
                                                    MiuixTheme.colorScheme.secondaryContainer,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.width(24.dp),
                                        )
                                        Text(
                                            text = word,
                                            style = MiuixTheme.textStyles.body1,
                                            color = MiuixTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    platformActions.copyToClipboard(seedPhrase)
                                    platformActions.showToast("Copied to clipboard")
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.action_copy_to_clipboard))
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.status_loading),
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.pallasync_join_chain),
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.pallasync_join_chain_desc),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    TextField(
                        value = enteredSeedPhrase,
                        onValueChange = { enteredSeedPhrase = it },
                        label = "Enter 24-word recovery phrase",
                        useLabelAsPlaceholder = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (enteredSeedPhrase.trim().split("\\s+".toRegex()).size == 24) {
                                isJoining = true
                                scope.launch {
                                    platformActions.showToast("Chain joined")
                                    onPairSuccess()
                                }
                            } else {
                                platformActions.showToast("Please enter a valid 24-word phrase")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining,
                    ) {
                        Text(
                            stringResource(
                                if (isJoining) R.string.pallasync_joining else R.string.pallasync_join_chain,
                            ),
                        )
                    }
                }
            }
        }
    }
}
