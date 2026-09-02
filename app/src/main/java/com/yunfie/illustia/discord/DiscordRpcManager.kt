package com.yunfie.illustia.discord

import android.os.Build
import com.my.kizzyrpc.KizzyRPC
import com.my.kizzyrpc.model.Activity
import com.my.kizzyrpc.model.Assets
import com.my.kizzyrpc.model.Metadata
import com.my.kizzyrpc.model.Timestamps
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DiscordRpcManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var rpc: KizzyRPC? = null
    private var activeToken: String? = null
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var currentArtworkId: Long? = null
    private var artworkOpenedTime: Long = System.currentTimeMillis()

    fun updatePresence(
        settings: AppSettings,
        selectedIllust: Illust?,
    ) {
        scope.launch {
            mutex.withLock {
                if (!isSupported() || !settings.discordRpcEnabled || settings.discordToken.isBlank()) {
                    closeInternal()
                    return@withLock
                }

                val token = settings.discordToken.trim()
                if (rpc == null || activeToken != token) {
                    closeInternal()
                    runCatching {
                        val newRpc = KizzyRPC(token)
                        activeToken = token
                        rpc = newRpc
                    }.onFailure {
                        rpc = null
                        activeToken = null
                        return@withLock
                    }
                }

                val currentRpc = rpc ?: return@withLock
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
                        val buttons = if (showButtons) listOf("Pixivで見る") else emptyList()
                        val metadata =
                            if (showButtons) {
                                Metadata(buttonUrls = listOf("https://www.pixiv.net/artworks/${selectedIllust.id}"))
                            } else {
                                null
                            }

                        Activity(
                            name = "Palleria",
                            details = title,
                            state = artist,
                            type = 0,
                            timestamps = Timestamps(start = artworkOpenedTime, end = null),
                            assets =
                                Assets(
                                    largeImage = "palleria_logo",
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
                            state = "アプリ内",
                            type = 0,
                            timestamps = Timestamps(start = sessionStartTime, end = null),
                            assets =
                                Assets(
                                    largeImage = "palleria_logo",
                                    largeText = "Palleria",
                                    smallImage = null,
                                    smallText = null,
                                ),
                            buttons = emptyList(),
                            metadata = null,
                            applicationId = appId,
                        )
                    }

                runCatching {
                    currentRpc.setActivity(activity, status = "online", since = 0L)
                }
            }
        }
    }

    fun close() {
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
        activeToken = null
        currentArtworkId = null
    }

    companion object {
        const val DEFAULT_DISCORD_APP_ID = "1544652855233744926"

        fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }
}
