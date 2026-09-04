package com.yunfie.illustia.discord

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.my.kizzyrpc.KizzyRPC
import com.my.kizzyrpc.model.Activity
import com.my.kizzyrpc.model.Assets
import com.my.kizzyrpc.model.Metadata
import com.my.kizzyrpc.model.Timestamps
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages Discord Rich Presence via KizzyRPC.
 *
 * Requirements & Features:
 * - Requires Android 8.1+ (API 27) and > 3GB RAM.
 * - Reuses the active KizzyRPC session through its public update API to avoid
 *   duplicate socket connections and Discord rate limits.
 * - Uses official "palleria_logo" asset registered in Discord Developer Portal.
 * - Silences uncaught KizzyRPC "Invalid" close exceptions on WebSocket threads.
 */
class DiscordRpcManager(
    private val appContext: Context? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var updateJob: Job? = null

    private var rpc: KizzyRPC? = null
    private var activeToken: String = ""

    private var sessionStart: Long = System.currentTimeMillis()
    private var currentArtworkId: Long? = null
    private var artworkStart: Long = System.currentTimeMillis()

    init {
        installExceptionSilencer()
    }

    fun updatePresence(
        settings: AppSettings,
        selectedIllust: Illust?,
    ) {
        updateJob?.cancel()
        updateJob =
            scope.launch {
                delay(600L) // Debounce rapid UI state updates

                mutex.withLock {
                    val token =
                        settings.discordToken
                            .trim()
                            .removeSurrounding("\"")
                            .removeSurrounding("'")

                    if (!isSupported(appContext) || !settings.discordRpcEnabled || token.isBlank()) {
                        recordDiagnostic(
                            when {
                                !isSupported(appContext) -> "開始不可: この端末は Rich Presence の要件を満たしていません"
                                !settings.discordRpcEnabled -> "停止: Rich Presence は無効です"
                                else -> "開始不可: Discord Token が設定されていません"
                            },
                        )
                        tearDown()
                        return@withLock
                    }

                    val appId = settings.discordApplicationId.trim().ifBlank { DEFAULT_APP_ID }
                    val showDetails = settings.discordRpcShowArtworkDetails
                    val showButtons = settings.discordRpcShowButtons

                    val activity =
                        if (selectedIllust != null) {
                            if (currentArtworkId != selectedIllust.id) {
                                currentArtworkId = selectedIllust.id
                                artworkStart = System.currentTimeMillis()
                            }
                            val detailText =
                                if (showDetails) {
                                    selectedIllust.title.take(128).ifBlank { "作品を閲覧中" }
                                } else {
                                    "作品を閲覧中"
                                }
                            val stateText =
                                if (showDetails) {
                                    "by ${selectedIllust.artistName}".take(128).ifBlank { "Palleria" }
                                } else {
                                    "Palleria"
                                }
                            val btns = if (showButtons) listOf("Pixivで見る") else null
                            val meta =
                                if (showButtons) {
                                    Metadata(buttonUrls = listOf("https://www.pixiv.net/artworks/${selectedIllust.id}"))
                                } else {
                                    null
                                }

                            Activity(
                                applicationId = appId,
                                name = "Palleria",
                                details = detailText,
                                state = stateText,
                                type = 0,
                                timestamps = Timestamps(start = artworkStart, end = null),
                                assets =
                                    Assets(
                                        largeImage = "palleria_logo",
                                        largeText = "Palleria",
                                        smallImage = null,
                                        smallText = null,
                                    ),
                                buttons = btns,
                                metadata = meta,
                            )
                        } else {
                            currentArtworkId = null
                            Activity(
                                applicationId = appId,
                                name = "Palleria",
                                details = "イラストを閲覧中",
                                state = "Palleria",
                                type = 0,
                                timestamps = Timestamps(start = sessionStart, end = null),
                                assets =
                                    Assets(
                                        largeImage = "palleria_logo",
                                        largeText = "Palleria",
                                        smallImage = null,
                                        smallText = null,
                                    ),
                                buttons = null,
                                metadata = null,
                            )
                        }

                    val now = System.currentTimeMillis()
                    val currentRpc = rpc
                    if (currentRpc != null && activeToken == token && currentRpc.isRpcRunning()) {
                        val updated =
                            withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                                currentRpc.updateRPC(
                                    activity = activity,
                                    status = "online",
                                    since = now,
                                )
                                true
                            } ?: false
                        if (updated) {
                            recordDiagnostic("Presence を既存接続へ送信しました: ${activity.details}")
                            return@withLock
                        }
                        recordDiagnostic("既存接続の更新がタイムアウトしたため、再接続します")
                    }

                    // Spin up new client if token changed or connection not active
                    tearDown()
                    recordDiagnostic("Discord Gateway に接続を開始しました (Application ID: $appId)")
                    try {
                        val newRpc = KizzyRPC(token)
                        rpc = newRpc
                        activeToken = token
                        val connected =
                            withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                                newRpc.setActivity(
                                    activity = activity,
                                    status = "online",
                                    since = now,
                                )
                                true
                            } ?: false
                        if (connected && newRpc.isRpcRunning()) {
                            recordDiagnostic("Discord Gateway に接続し、Presence を送信しました")
                        } else {
                            recordDiagnostic("接続がタイムアウトしました。Token、ネットワーク、Discord 側の制限を確認してください")
                            tearDown()
                        }
                    } catch (error: CancellationException) {
                        // A newer UI state superseded this request. This is expected and is not a connection error.
                        throw error
                    } catch (error: Exception) {
                        Log.e(TAG, "Failed to start KizzyRPC", error)
                        recordDiagnostic("Discord 接続エラー: ${error.safeDiagnosticMessage()}")
                        tearDown()
                    }
                }
            }
    }

    fun close() {
        updateJob?.cancel()
        scope.launch {
            mutex.withLock {
                tearDown()
            }
        }
    }

    private fun tearDown() {
        runCatching {
            rpc?.closeRPC()
        }
        rpc = null
        activeToken = ""
        currentArtworkId = null
    }

    private fun recordDiagnostic(message: String) {
        val entry = "${DIAGNOSTIC_TIME_FORMAT.format(Date())}  $message"
        Log.d(TAG, entry)
        _diagnostics.value = (_diagnostics.value + entry).takeLast(MAX_DIAGNOSTIC_ENTRIES)
    }

    private fun Throwable.safeDiagnosticMessage(): String =
        message
            ?.replace(Regex("(?i)(token|authorization)\\s*[:=]\\s*[^,\\s]+"), "$1=[redacted]")
            ?.take(MAX_ERROR_MESSAGE_LENGTH)
            ?: javaClass.simpleName

    companion object {
        private const val TAG = "DiscordRpcManager"
        const val DEFAULT_APP_ID = "1544652855233744926"
        private const val CONNECTION_TIMEOUT_MS = 15_000L
        private const val MAX_DIAGNOSTIC_ENTRIES = 20
        private const val MAX_ERROR_MESSAGE_LENGTH = 240
        private val DIAGNOSTIC_TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private val _diagnostics = MutableStateFlow<List<String>>(emptyList())
        val diagnostics: StateFlow<List<String>> = _diagnostics.asStateFlow()

        fun clearDiagnostics() {
            _diagnostics.value = emptyList()
        }

        /** KizzyRPC requires Android 8.1 (API 27) minimum and more than 3GB of RAM. */
        fun isSupported(context: Context? = null): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return false
            if (context != null && !hasSufficientRam(context)) return false
            return true
        }

        fun hasSufficientRam(context: Context): Boolean {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return true
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            // 3GB RAM devices typically report ~2.8GB - 3.0GB totalMem due to hardware reserves.
            // 4GB+ RAM devices report >= ~3.5GB totalMem.
            // Requiring totalMem > 3.2 GB ensures 3GB and lower devices are excluded.
            return memInfo.totalMem > 3_200_000_000L
        }

        @Volatile
        private var silencerInstalled = false

        private fun installExceptionSilencer() {
            if (silencerInstalled) return
            silencerInstalled = true
            val prevHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                if (throwable is RuntimeException && throwable.message == "Invalid" &&
                    thread.name.contains("WebSocket", ignoreCase = true)
                ) {
                    Log.w(TAG, "Suppressed KizzyRPC unhandled thread exception on ${thread.name}")
                    return@setDefaultUncaughtExceptionHandler
                }
                prevHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
