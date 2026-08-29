package com.yunfie.illustia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfie.illustia.*
import com.yunfie.illustia.platform.LocalPlatformActions
import com.yunfie.illustia.ui.components.BottomSheetInsideMargin
import com.yunfie.illustia.ui.components.ElevatedPanel
import com.yunfie.illustia.ui.components.HeaderIcon
import com.yunfie.illustia.ui.components.LocalBottomSheetBackgroundColor
import com.yunfie.illustia.ui.components.NonAmoledDarkTheme
import com.yunfie.illustia.ui.components.PredictiveBackGestureHandler
import com.yunfie.illustia.ui.components.StateBanner
import com.yunfie.illustia.ui.components.overlayActionButtonColors
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Import
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun RefreshTokenLoginBottomSheet(
    state: IllustiaUiState,
    viewModel: IllustiaViewModel,
    onDismiss: () -> Unit,
) {
    OverlayBottomSheet(
        show = true,
        modifier = Modifier.scrollEndHaptic(),
        title = stringResource(R.string.login_token_title),
        onDismissRequest = onDismiss,
        backgroundColor = LocalBottomSheetBackgroundColor.current,
        insideMargin = BottomSheetInsideMargin,
        startAction = {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = MiuixIcons.Close, contentDescription = stringResource(R.string.action_close))
            }
        },
    ) {
        NonAmoledDarkTheme {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TextField(
                    value = state.settings.refreshToken,
                    onValueChange = viewModel::updateRefreshToken,
                    label = stringResource(R.string.login_refresh_token_label),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = viewModel::login,
                    enabled = state.settings.refreshToken.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = overlayActionButtonColors(),
                ) {
                    Text(stringResource(R.string.login_token_button))
                }
                StateBanner(state.loadState)
            }
        }
    }
}

@Composable
fun PixivWebLoginScreen(
    request: PixivWebLoginRequest,
    onCodeReceived: (String) -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
    onWebViewChanged: (Any?) -> Unit = {},
) {
    val platformActions = LocalPlatformActions.current
    var callbackUrlInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(request.authorizationUrl) {
        platformActions.openUrl(request.authorizationUrl)
    }

    PredictiveBackGestureHandler(onBack = onCancel)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.login_web_description),
            style = MiuixTheme.textStyles.title2,
            fontWeight = FontWeight.Bold,
        )

        Button(
            onClick = {
                platformActions.openUrl(request.authorizationUrl)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = overlayActionButtonColors(),
        ) {
            Text(stringResource(R.string.detail_open_in_browser))
        }

        TextField(
            value = callbackUrlInput,
            onValueChange = {
                callbackUrlInput = it
                errorMessage = null
                val code = pixivLoginCodeOrNull(it)
                if (code != null) {
                    onCodeReceived(code)
                }
            },
            label = "pixiv://...",
            useLabelAsPlaceholder = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = MiuixTheme.colorScheme.error,
                style = MiuixTheme.textStyles.body2,
            )
        }

        Button(
            onClick = {
                val code = pixivLoginCodeOrNull(callbackUrlInput)
                if (code != null) {
                    onCodeReceived(code)
                } else {
                    errorMessage = "Invalid callback URL"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = callbackUrlInput.isNotBlank(),
        ) {
            Text(stringResource(R.string.action_confirm))
        }

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_cancel))
        }
    }
}

private fun pixivLoginCodeOrNull(url: String): String? {
    val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return null
    val queryParams =
        uri.query?.split("&")?.associate {
            val parts = it.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        } ?: emptyMap()
    val code = queryParams["code"] ?: return null
    val scheme = uri.scheme
    val host = uri.host
    val path = uri.path
    val isPixivLoginRedirect = scheme == "pixiv" && host == "account" && path == "/login"
    val isPixivCallback =
        scheme == "https" &&
            host == "app-api.pixiv.net" &&
            path == "/web/v1/users/auth/pixiv/callback"
    val isPixivCodeUrl = host?.contains("pixiv", ignoreCase = true) == true && code.isNotBlank()
    return if (isPixivLoginRedirect || isPixivCallback || isPixivCodeUrl) code else null
}

@Composable
fun ScreenHeader(
    title: String,
    meta: String,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actionIcon?.let {
                HeaderIcon(it, onClick = onActionClick)
            }
            if (trailingIcon != null) {
                Spacer(Modifier.width(16.dp))
                HeaderIcon(trailingIcon, onClick = onTrailingClick)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                style = MiuixTheme.textStyles.title1,
            )
            Text(
                text = meta,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontWeight = FontWeight.Bold,
                style = MiuixTheme.textStyles.main,
            )
        }
    }
}
