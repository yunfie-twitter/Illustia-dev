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

class DiscordRpcManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var updateJob: Job? = null
    private var rpc: KizzyRPC? = null
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var currentArtworkId: Long? = null
    private var artworkOpenedTime: Long = System.currentTimeMillis()

    fun updatePresence(
        settings: AppSettings,
        selectedIllust: Illust?,
    ) {
        updateJob?.cancel()
        updateJob =
            scope.launch {
                delay(600) // Debounce rapid state changes to respect Discord Gateway identify rate limits
                mutex.withLock {
                    val rawToken =
                        settings.discordToken
                            .trim()
                            .removeSurrounding("\"")
                            .removeSurrounding("'")
                    if (!isSupported() || !settings.discordRpcEnabled || rawToken.isBlank()) {
                        Log.d(
                            TAG,
                            "Discord RPC disabled or unsupported: isSupported=${isSupported()}, enabled=${settings.discordRpcEnabled}, hasToken=${rawToken.isNotBlank()}",
                        )
                        closeInternal()
                        return@withLock
                    }

                    closeInternal()

                    val appId = settings.discordApplicationId.trim().ifBlank { DEFAULT_DISCORD_APP_ID }
                    val activity =
                        if (selectedIllust != null) {
                            if (currentArtworkId != selectedIllust.id) {
                                currentArtworkId = selectedIllust.id
                                artworkOpenedTime = System.currentTimeMillis()
                            }
                            val showDetails = settings.discordRpcShowArtworkDetails
                            val showButtons = settings.discordRpcShowButtons
                            val title = if (showDetails) selectedIllust.title.take(128) else "作品を閲覧中"
                            val artist = if (showDetails) "by ${selectedIllust.artistName}".take(128) else "Palleria"
                            val buttons = if (showButtons) listOf("Pixivで見る") else null
                            val metadata =
                                if (showButtons) {
                                    Metadata(buttonUrls = listOf("https://www.pixiv.net/artworks/${selectedIllust.id}"))
                                } else {
                                    null
                                }

                            Activity(
                                name = "Palleria",
                                details = title.ifBlank { "作品を閲覧中" },
                                state = artist.ifBlank { "Palleria" },
                                type = 0,
                                timestamps = Timestamps(start = artworkOpenedTime, end = null),
                                assets =
                                    Assets(
                                        largeImage = null,
                                        largeText = "Palleria",
                                        smallImage = null,
                                        smallText = null,
                                    ),
                                buttons = buttons,
                                metadata = metadata,
                                applicationId = appId,
                            )
                        } else {
                            currentArtworkId = null
                            Activity(
                                name = "Palleria",
                                details = "イラストを閲覧中",
                                state = "Palleria",
                                type = 0,
                                timestamps = Timestamps(start = sessionStartTime, end = null),
                                assets =
                                    Assets(
                                        largeImage = null,
                                        largeText = "Palleria",
                                        smallImage = null,
                                        smallText = null,
                                    ),
                                buttons = null,
                                metadata = null,
                                applicationId = appId,
                            )
                        }

                    runCatching {
                        Log.d(TAG, "Connecting KizzyRPC with appId=$appId, activity=${activity.details} / ${activity.state}")
                        val newRpc = KizzyRPC(rawToken)
                        rpc = newRpc
                        newRpc.setActivity(activity, status = "online", since = 0L)
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to start Discord RPC", error)
                        closeInternal()
                    }
                }
            }
    }

    fun close() {
        updateJob?.cancel()
        scope.launch {
            mutex.withLock {
                closeInternal()
            }
        }
    }

    private fun closeInternal() {
        runCatching {
            rpc?.closeRPC()
        }
        rpc = null
        currentArtworkId = null
    }

    companion object {
        private const val TAG = "DiscordRpcManager"
        const val DEFAULT_DISCORD_APP_ID = "1544652855233744926"

        fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }
}
