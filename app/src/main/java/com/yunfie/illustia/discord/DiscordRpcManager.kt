package com.yunfie.illustia.discord

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages Discord Rich Presence via KizzyRPC.
 *
 * Notes on KizzyRPC behaviour (v1.0.71):
 * - [KizzyRPC] must NOT be recreated on every presence update; recreating triggers a new
 *   WebSocket IDENTIFY which Discord rate-limits (max 1000/day per account). Reuse the same
 *   instance and call [KizzyRPC.setActivity] to update.
 * - [KizzyRPC] must be recreated when the token changes.
 * - `since` in [KizzyRPC.setActivity] must be a Unix epoch millis (not 0).
 * - [Assets.largeImage] accepts either a Discord application asset key (registered in the
 *   Developer Portal) or an "mp:" attachment URL. Passing `null` causes the RPC payload to
 *   omit the assets block entirely – which is fine for Discord.
 * - [Activity.applicationId] is REQUIRED when [Activity.buttons] is non-null; otherwise
 *   Discord silently drops the buttons.
 */
class DiscordRpcManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var updateJob: Job? = null

    /** KizzyRPC WebSocket client – reused across [setActivity] calls. */
    private var rpc: KizzyRPC? = null
    private var activeToken: String = ""

    private var sessionStart: Long = System.currentTimeMillis()
    private var currentArtworkId: Long? = null
    private var artworkStart: Long = System.currentTimeMillis()

    fun updatePresence(
        settings: AppSettings,
        selectedIllust: Illust?,
    ) {
        updateJob?.cancel()
        updateJob =
            scope.launch {
                // Debounce: wait briefly before acting to absorb rapid recompositions.
                delay(800L)

                mutex.withLock {
                    val token =
                        settings.discordToken
                            .trim()
                            .removeSurrounding("\"")
                            .removeSurrounding("'")

                    if (!isSupported() || !settings.discordRpcEnabled || token.isBlank()) {
                        Log.d(TAG, "RPC disabled or unsupported – closing")
                        tearDown()
                        return@withLock
                    }

                    // If the token changed (or we have no RPC yet), spin up a fresh client.
                    if (rpc == null || activeToken != token) {
                        tearDown()
                        Log.d(TAG, "Creating new KizzyRPC instance")
                        runCatching { rpc = KizzyRPC(token) }
                            .onFailure { e ->
                                Log.e(TAG, "Failed to create KizzyRPC", e)
                                rpc = null
                                activeToken = ""
                                return@withLock
                            }
                        activeToken = token
                    }

                    val currentRpc = rpc ?: return@withLock
                    val appId = settings.discordApplicationId.trim().ifBlank { DEFAULT_APP_ID }
                    val showDetails = settings.discordRpcShowArtworkDetails
                    val showButtons = settings.discordRpcShowButtons

                    val activity =
                        if (selectedIllust != null) {
                            if (currentArtworkId != selectedIllust.id) {
                                currentArtworkId = selectedIllust.id
                                artworkStart = System.currentTimeMillis()
                            }
                            val detailText = if (showDetails) selectedIllust.title.take(128) else "作品を閲覧中"
                            val stateText = if (showDetails) "by ${selectedIllust.artistName}".take(128) else "Palleria"
                            val btns = if (showButtons) listOf("Pixivで見る") else null
                            val meta =
                                if (showButtons) {
                                    Metadata(
                                        buttonUrls = listOf("https://www.pixiv.net/artworks/${selectedIllust.id}"),
                                    )
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
                                assets = null,
                                buttons = btns,
                                metadata = meta,
                            )
                        } else {
                            currentArtworkId = null
                            Activity(
                                applicationId = appId,
                                name = "Palleria",
                                details = "イラストを閲覧中",
                                state = null,
                                type = 0,
                                timestamps = Timestamps(start = sessionStart, end = null),
                                assets = null,
                                buttons = null,
                                metadata = null,
                            )
                        }

                    Log.d(
                        TAG,
                        "setActivity: name=${activity.name}, details=${activity.details}, state=${activity.state}, appId=${activity.applicationId}",
                    )
                    runCatching {
                        currentRpc.setActivity(
                            activity = activity,
                            status = "online",
                            since = System.currentTimeMillis(),
                        )
                    }.onFailure { e ->
                        Log.e(TAG, "setActivity failed – resetting RPC client", e)
                        tearDown()
                    }
                }
            }
    }

    fun close() {
        updateJob?.cancel()
        scope.launch { mutex.withLock { tearDown() } }
    }

    private fun tearDown() {
        runCatching { rpc?.closeRPC() }
        rpc = null
        activeToken = ""
        currentArtworkId = null
    }

    companion object {
        private const val TAG = "DiscordRpcManager"
        const val DEFAULT_APP_ID = "1544652855233744926"

        /** KizzyRPC requires Android 8.1 (API 27) minimum. */
        fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }
}
