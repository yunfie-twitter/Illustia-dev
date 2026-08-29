package com.yunfie.illustia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yunfie.illustia.*
import com.yunfie.illustia.platform.LocalPlatformActions
import com.yunfie.illustia.ui.components.ElevatedPanel
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import com.yunfie.illustia.ui.components.SettingLinkRow
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Timer
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class SettingsCategory(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val route: () -> Unit,
)

@Composable
fun SettingsScreen(
    state: IllustiaUiState,
    viewModel: IllustiaViewModel,
    onBack: () -> Unit,
) {
    PredictiveBackGestureHandler(onBack = onBack)
    val platformActions = LocalPlatformActions.current
    val appVersion = remember { platformActions.getAppVersionName() }
    val scrollBehavior = MiuixScrollBehavior()
    val mutedTotal = state.settings.mutedIllusts.size + state.settings.mutedUsers.size + state.settings.mutedTags.size

    val generalTitle = stringResource(R.string.settings_general)
    val generalSummary = stringResource(R.string.settings_general_summary)
    val imageTitle = stringResource(R.string.settings_image)
    val imageSummary = stringResource(R.string.settings_image_summary)
    val bookmarkTitle = stringResource(R.string.settings_bookmark)
    val bookmarkSummary = stringResource(R.string.settings_bookmark_summary)
    val accountTitle = stringResource(R.string.settings_account)
    val loggedInSummary = stringResource(R.string.settings_logged_in)
    val notLoggedInSummary = stringResource(R.string.settings_not_logged_in)
    val dataTitle = stringResource(R.string.settings_data)
    val viewHistoryTitle = stringResource(R.string.more_view_history)
    val itemsCountTemplate = stringResource(R.string.data_items_count, state.settings.viewHistory.size)
    val muteSettingsTitle = stringResource(R.string.more_mute_settings)
    val muteItemsCountTemplate = stringResource(R.string.data_items_count, mutedTotal)
    val privacyTitle = stringResource(R.string.privacy_mode_title)
    val privacyEnabled = stringResource(R.string.privacy_settings_enabled)
    val privacyDisabled = stringResource(R.string.privacy_settings_disabled)
    val experimentalTitle = stringResource(R.string.experimental_settings_title)
    val experimentalSummary = stringResource(R.string.settings_experimental_summary)

    val categories =
        remember(
            state.settings.refreshToken,
            state.settings.viewHistory.size,
            mutedTotal,
            state.settings.privacyModeEnabled,
            generalTitle,
            generalSummary,
            imageTitle,
            imageSummary,
            bookmarkTitle,
            bookmarkSummary,
            accountTitle,
            loggedInSummary,
            notLoggedInSummary,
            dataTitle,
            viewHistoryTitle,
            itemsCountTemplate,
            muteSettingsTitle,
            muteItemsCountTemplate,
            privacyTitle,
            privacyEnabled,
            privacyDisabled,
            experimentalTitle,
            experimentalSummary,
        ) {
            listOf(
                SettingsCategory(
                    generalTitle,
                    generalSummary,
                    MiuixIcons.More,
                ) {
                    viewModel.openGeneralSettings()
                },
                SettingsCategory(
                    imageTitle,
                    imageSummary,
                    MiuixIcons.Photos,
                ) {
                    viewModel.openImageSettings()
                },
                SettingsCategory(
                    bookmarkTitle,
                    bookmarkSummary,
                    MiuixIcons.FavoritesFill,
                ) {
                    viewModel.openBookmarkSettings()
                },
                SettingsCategory(
                    accountTitle,
                    if (state.settings.refreshToken.isNotBlank()) loggedInSummary else notLoggedInSummary,
                    MiuixIcons.Contacts,
                ) {
                    viewModel.openAccountSettings()
                },
                SettingsCategory(
                    dataTitle,
                    "$viewHistoryTitle $itemsCountTemplate / $muteSettingsTitle $muteItemsCountTemplate",
                    MiuixIcons.Timer,
                ) {
                    viewModel.openDataSettings()
                },
                SettingsCategory(
                    privacyTitle,
                    if (state.settings.privacyModeEnabled) privacyEnabled else privacyDisabled,
                    MiuixIcons.Lock,
                ) { viewModel.openPrivacyModeSettings() },
                SettingsCategory(
                    experimentalTitle,
                    experimentalSummary,
                    MiuixIcons.Settings,
                ) { viewModel.openExperimentalSettings() },
            )
        }

    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(R.string.settings_title),
                largeTitle = stringResource(R.string.settings_title),
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
            item {
                ElevatedPanel(contentPadding = PaddingValues(0.dp)) {
                    categories.forEachIndexed { index, cat ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            )
                        }
                        SettingLinkRow(
                            title = cat.title,
                            summary = cat.summary,
                            icon = cat.icon,
                            onClick = cat.route,
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "${stringResource(R.string.app_name)} v$appVersion",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                        style = MiuixTheme.textStyles.footnote1,
                    )
                    Text(
                        stringResource(R.string.settings_footer),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f),
                        style = MiuixTheme.textStyles.footnote1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
