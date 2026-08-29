package com.yunfie.illustia.settings

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest

class DesktopSettingsStore : SettingsStore {
    private val mutex = Mutex()
    private var currentSettings: AppSettings = AppSettings()
    private var pinHash: String? = null
    private var unlockCodeHash: String? = null

    override suspend fun read(): AppSettings = mutex.withLock { currentSettings }

    override suspend fun readStartup(): AppSettings = mutex.withLock { currentSettings }

    override suspend fun write(settings: AppSettings, baseSettings: AppSettings?): AppSettings =
        mutex.withLock {
            currentSettings = settings
            settings
        }

    override suspend fun writeSyncedCollections(synced: SyncedCollectionsSnapshot) {
        mutex.withLock {
            currentSettings = currentSettings.withSyncedCollections(synced)
        }
    }

    override suspend fun clearSensitive() {
        mutex.withLock {
            pinHash = null
            unlockCodeHash = null
        }
    }

    override fun hasPinSet(): Boolean = !pinHash.isNullOrBlank()

    override fun verifyPin(pin: String): Boolean {
        val current = pinHash ?: return false
        return hash(pin) == current
    }

    override fun savePinHash(pin: String) {
        pinHash = hash(pin)
    }

    override fun clearPinHash() {
        pinHash = null
    }

    override fun hasUnlockCodeSet(): Boolean = !unlockCodeHash.isNullOrBlank()

    override fun verifyUnlockCode(code: String): Boolean {
        val current = unlockCodeHash ?: return false
        return hash(code) == current
    }

    override fun isValidUnlockCode(code: String): Boolean = code.length >= 3

    override fun saveUnlockCodeHash(code: String) {
        unlockCodeHash = hash(code)
    }

    override fun clearUnlockCodeHash() {
        unlockCodeHash = null
    }

    override fun setPallaSyncEnabledFromCoordinator(enabled: Boolean) {
        currentSettings = currentSettings.copy(pallaSyncEnabled = enabled)
    }

    override fun getSavedIllustStorageBytes(): Long = 0L

    override fun savedIllustDir(): File? = null

    private fun hash(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
