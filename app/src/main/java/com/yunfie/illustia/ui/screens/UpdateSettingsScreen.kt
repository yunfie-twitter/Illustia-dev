package com.yunfie.illustia.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yunfie.illustia.IllustiaUiState
import com.yunfie.illustia.IllustiaViewModel
import com.yunfie.illustia.R
import com.yunfie.illustia.ui.components.DividerLine
import com.yunfie.illustia.ui.components.ElevatedPanel
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.LoadingIndicator
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import com.yunfie.illustia.ui.components.Section
import com.yunfie.illustia.ui.components.SettingLinkRow
import com.yunfie.illustia.ui.components.SettingRow
import com.yunfie.illustia.ui.components.SettingSwitchRow
import com.yunfie.illustia.updater.UpdateCheckState
import com.yunfie.illustia.updater.UpdateInstallMethod
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun UpdateSettingsScreen(
    state: IllustiaUiState,
    viewModel: IllustiaViewModel,
    onBack: () -> Unit,
) {
    PredictiveBackGestureHandler(onBack = onBack)
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val updateState by viewModel.updateCheckState.collectAsStateWithLifecycle()
    val isShizukuAvailable = remember { viewModel.appUpdaterRepository.isShizukuAvailable() }
    val isShizukuGranted = remember { viewModel.appUpdaterRepository.isShizukuPermissionGranted() }
    val currentVersionName = remember { viewModel.appUpdaterRepository.getCurrentVersionName() }

    val installMethods =
        listOf(
            stringResource(R.string.update_method_standard),
            stringResource(R.string.update_method_shizuku),
        )
    val selectedInstallIndex =
        if (state.settings.updateInstallMethod == UpdateInstallMethod.SHIZUKU.value) 1 else 0

    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.update_settings_title),
                largeTitle = stringResource(R.string.update_settings_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    HeaderIcon(MiuixIcons.Back, onClick = onBack)
                },
            )
        },
    ) { scaffoldPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .background(MiuixTheme.colorScheme.surface),
            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = scaffoldPadding.calculateTopPadding() + 16.dp,
                    bottom = 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // インストール設定セクション
            item {
                Section(stringResource(R.string.update_section_install)) {
                    ElevatedPanel {
                        OverlayDropdownPreference(
                            title = stringResource(R.string.update_install_method),
                            summary =
                                when (selectedInstallIndex) {
                                    1 -> stringResource(R.string.update_method_shizuku_desc)
                                    else -> stringResource(R.string.update_method_standard_desc)
                                },
                            items = installMethods,
                            selectedIndex = selectedInstallIndex,
                            onSelectedIndexChange = { index ->
                                val method =
                                    if (index == 1) UpdateInstallMethod.SHIZUKU else UpdateInstallMethod.STANDARD_APK
                                viewModel.updateUpdateInstallMethod(method)
                            },
                        )

                        if (selectedInstallIndex == 1) {
                            DividerLine()
                            SettingRow(
                                title = "Shizuku",
                                summary =
                                    when {
                                        !isShizukuAvailable -> stringResource(R.string.update_shizuku_not_running)
                                        !isShizukuGranted -> stringResource(R.string.update_shizuku_permission_required)
                                        else -> stringResource(R.string.update_shizuku_available)
                                    },
                            ) {
                                if (isShizukuAvailable && !isShizukuGranted) {
                                    Button(
                                        onClick = { viewModel.appUpdaterRepository.requestShizukuPermission() },
                                        colors = ButtonDefaults.buttonColorsPrimary(),
                                        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    ) {
                                        Text("許可", color = MiuixTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }

                        DividerLine()
                        SettingSwitchRow(
                            title = stringResource(R.string.update_auto_check_startup),
                            summary = stringResource(R.string.update_auto_check_startup_desc),
                            checked = state.settings.autoCheckUpdateOnStartup,
                            onCheckedChange = viewModel::updateAutoCheckUpdateOnStartup,
                        )
                    }
                }
            }

            // アップデートの確認セクション
            item {
                Section(stringResource(R.string.update_section_check)) {
                    ElevatedPanel {
                        SettingRow(
                            title = stringResource(R.string.about_version_label),
                            summary = currentVersionName,
                        ) {
                            Button(
                                onClick = { viewModel.checkForUpdates(silent = false) },
                                enabled = updateState !is UpdateCheckState.Checking && updateState !is UpdateCheckState.Downloading,
                                colors = ButtonDefaults.buttonColorsPrimary(),
                                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    stringResource(R.string.update_check_button),
                                    color = MiuixTheme.colorScheme.onPrimary,
                                    style = MiuixTheme.textStyles.footnote1,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        when (val currentUpdate = updateState) {
                            is UpdateCheckState.Checking -> {
                                DividerLine()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LoadingIndicator(modifier = Modifier.size(24.dp))
                                    Text(
                                        stringResource(R.string.update_checking),
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        style = MiuixTheme.textStyles.body2,
                                    )
                                }
                            }

                            is UpdateCheckState.UpToDate -> {
                                DividerLine()
                                Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(
                                        stringResource(R.string.update_up_to_date, currentUpdate.currentVersion),
                                        color = MiuixTheme.colorScheme.primary,
                                        style = MiuixTheme.textStyles.body2,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            is UpdateCheckState.UpdateAvailable -> {
                                val release = currentUpdate.release
                                DividerLine()
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.update_available_title, release.versionName),
                                        color = MiuixTheme.colorScheme.onBackground,
                                        style = MiuixTheme.textStyles.title2,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (release.releaseTitle.isNotBlank() && release.releaseTitle != release.versionName) {
                                        Text(
                                            release.releaseTitle,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            style = MiuixTheme.textStyles.body2,
                                        )
                                    }
                                    if (release.releaseNotes.isNotBlank()) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MiuixTheme.colorScheme.surfaceContainer)
                                                    .padding(10.dp),
                                        ) {
                                            Text(
                                                release.releaseNotes,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                style = MiuixTheme.textStyles.footnote1,
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.downloadUpdate(release) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColorsPrimary(),
                                    ) {
                                        Text(
                                            stringResource(R.string.update_download_button),
                                            color = MiuixTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }

                            is UpdateCheckState.Downloading -> {
                                DividerLine()
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val downloadedMb =
                                        String.format(
                                            java.util.Locale.US,
                                            "%.1f",
                                            currentUpdate.downloadedBytes / (1024f * 1024f),
                                        )
                                    val totalMb =
                                        String.format(
                                            java.util.Locale.US,
                                            "%.1f",
                                            currentUpdate.totalBytes / (1024f * 1024f),
                                        )
                                    Text(
                                        stringResource(R.string.update_downloading, "MB", "MB"),
                                        color = MiuixTheme.colorScheme.onBackground,
                                        style = MiuixTheme.textStyles.body2,
                                    )
                                    LinearProgressIndicator(
                                        progress = currentUpdate.progress,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            is UpdateCheckState.ReadyToInstall -> {
                                DividerLine()
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.update_ready_to_install),
                                        color = MiuixTheme.colorScheme.primary,
                                        style = MiuixTheme.textStyles.body2,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Button(
                                        onClick = { viewModel.installUpdate(currentUpdate.apkFile) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColorsPrimary(),
                                    ) {
                                        Text(
                                            stringResource(R.string.update_install_button),
                                            color = MiuixTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }

                            is UpdateCheckState.Installing -> {
                                DividerLine()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LoadingIndicator(modifier = Modifier.size(24.dp))
                                    Text(
                                        stringResource(R.string.update_installing),
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        style = MiuixTheme.textStyles.body2,
                                    )
                                }
                            }

                            is UpdateCheckState.Error -> {
                                DividerLine()
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        currentUpdate.message,
                                        color = MiuixTheme.colorScheme.error,
                                        style = MiuixTheme.textStyles.footnote1,
                                    )
                                }
                            }

                            UpdateCheckState.Idle -> {
                                // No action needed
                            }
                        }
                    }
                }
            }

            item {
                Section(stringResource(R.string.about_section_links)) {
                    ElevatedPanel {
                        SettingLinkRow(stringResource(R.string.update_view_on_github)) {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/yunfie-twitter/Palleria/releases"),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
