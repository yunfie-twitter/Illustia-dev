package com.yunfie.illustia.pallasync

import com.yunfie.illustia.models.Illust
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PalleriaSyncManager {
    fun normalizeServerUrl(url: String): String? {
        val trimmed = url.trim().trimEnd('/')
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else null
    }

    fun getServerUrl(): String = ""
    suspend fun initializeGenesis(serverUrl: String): String = ""
    suspend fun deleteChain(callApi: Boolean = true): Boolean = true
    suspend fun syncNow(): Boolean = true
    suspend fun getDeviceViewHistory(deviceId: String): List<Illust> = emptyList()
    suspend fun fetchDevices(serverUrl: String, chainId: String): List<Any> = emptyList()

    companion object {
        private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
        val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()
    }
}
