package com.yunfie.illustia.settings

import com.yunfie.illustia.models.SavedIllustItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class SavedIllustWithPages(
    val illust: SavedIllustItem,
    val pages: List<String> = emptyList(),
)

data class SettingsSyncUpdate(
    val revision: Long,
    val collections: SyncedCollectionsSnapshot,
)

data class PallaSyncEnabledUpdate(
    val revision: Long,
    val enabled: Boolean,
)

interface SettingsStore {
    suspend fun read(): AppSettings
    suspend fun readStartup(): AppSettings
    suspend fun write(settings: AppSettings, baseSettings: AppSettings? = null): AppSettings
    suspend fun writeSyncedCollections(synced: SyncedCollectionsSnapshot)
    suspend fun clearSensitive()
    fun hasPinSet(): Boolean
    fun verifyPin(pin: String): Boolean
    fun savePinHash(pin: String)
    fun clearPinHash()
    fun hasUnlockCodeSet(): Boolean
    fun verifyUnlockCode(code: String): Boolean
    fun isValidUnlockCode(code: String): Boolean
    fun saveUnlockCodeHash(code: String)
    fun clearUnlockCodeHash()
    fun setPallaSyncEnabledFromCoordinator(enabled: Boolean)
    fun getSavedIllustStorageBytes(): Long = 0L
    fun savedIllustDir(): File? = null
    suspend fun getSavedIllusts(): List<SavedIllustItem> = emptyList()
    suspend fun getSavedIllust(illustId: Long): SavedIllustWithPages? = null
    suspend fun deleteSavedIllust(illustId: Long) {}
    suspend fun insertSavedIllust(illust: SavedIllustItem, pages: List<String>) {}

    companion object {
        private val _syncUpdates = MutableStateFlow<SettingsSyncUpdate?>(null)
        val syncUpdates: StateFlow<SettingsSyncUpdate?> = _syncUpdates.asStateFlow()

        private val _pallaSyncEnabledUpdates = MutableStateFlow<PallaSyncEnabledUpdate?>(null)
        val pallaSyncEnabledUpdates: StateFlow<PallaSyncEnabledUpdate?> = _pallaSyncEnabledUpdates.asStateFlow()

        fun notifySyncUpdate(update: SettingsSyncUpdate) {
            _syncUpdates.value = update
        }

        fun notifyPallaSyncEnabledUpdate(update: PallaSyncEnabledUpdate) {
            _pallaSyncEnabledUpdates.value = update
        }
    }
}
