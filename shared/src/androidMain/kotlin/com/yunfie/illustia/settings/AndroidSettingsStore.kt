package com.yunfie.illustia.settings

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okio.Path.Companion.toPath
import com.yunfie.illustia.models.SavedIllustItem
import com.yunfie.illustia.pallasync.PallaSyncEventWriter
import com.yunfie.illustia.pallasync.PallaSyncPendingEvent
import com.yunfie.illustia.pallasync.PalleriaSyncCoordinator
import com.yunfie.illustia.pallasync.PalleriaSyncManager
import com.yunfie.illustia.pallasync.buildSettingsSyncEvents
import com.yunfie.illustia.settings.db.IllustiaDatabase
import com.yunfie.illustia.settings.db.SavedIllustEntity
import com.yunfie.illustia.settings.db.SavedIllustPageEntity
import com.yunfie.illustia.settings.store.DATASTORE_NAME
import com.yunfie.illustia.settings.store.KEY_APP_LANGUAGE
import com.yunfie.illustia.settings.store.KEY_PERFORMANCE_MODE
import com.yunfie.illustia.settings.store.LEGACY_PREFS_NAME
import com.yunfie.illustia.settings.store.PALLA_SYNC_ENABLED
import com.yunfie.illustia.settings.store.PALLA_SYNC_SERVER_URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import com.yunfie.illustia.settings.store.clearPinHash as clearPinHashImpl
import com.yunfie.illustia.settings.store.clearSensitiveSettings as clearSensitiveSettingsImpl
import com.yunfie.illustia.settings.store.clearUnlockCodeHash as clearUnlockCodeHashImpl
import com.yunfie.illustia.settings.store.hasPinSet as hasPinSetImpl
import com.yunfie.illustia.settings.store.hasUnlockCodeSet as hasUnlockCodeSetImpl
import com.yunfie.illustia.settings.store.isValidUnlockCode as isValidUnlockCodeImpl
import com.yunfie.illustia.settings.store.migrateSettingsIfNeeded as migrateSettingsIfNeededImpl
import com.yunfie.illustia.settings.store.readAppSettings as readAppSettingsImpl
import com.yunfie.illustia.settings.store.readStartupAppSettings as readStartupAppSettingsImpl
import com.yunfie.illustia.settings.store.savePinHash as savePinHashImpl
import com.yunfie.illustia.settings.store.saveUnlockCodeHash as saveUnlockCodeHashImpl
import com.yunfie.illustia.settings.store.savedIllustStorageBytes as savedIllustStorageBytesImpl
import com.yunfie.illustia.settings.store.verifyPinHash as verifyPinHashImpl
import com.yunfie.illustia.settings.store.verifyUnlockCodeHash as verifyUnlockCodeHashImpl
import com.yunfie.illustia.settings.store.writeAppSettings as writeAppSettingsImpl
import com.yunfie.illustia.settings.store.writeSyncedCollections as writeSyncedCollectionsImpl

class AndroidSettingsStore internal constructor(
    context: Context,
    private val syncEventWriter: PallaSyncEventWriter,
) : SettingsStore {
    constructor(context: Context) : this(
        context,
        object : PallaSyncEventWriter {
            private val delegate by lazy { PalleriaSyncCoordinator(context = context.applicationContext) }
            override suspend fun enqueueDataEvents(events: List<PallaSyncPendingEvent>): Boolean =
                if (events.isEmpty()) true else delegate.enqueueDataEvents(events)

            override suspend fun <T> enqueueDataEventsThen(
                events: List<PallaSyncPendingEvent>,
                afterEnqueue: suspend () -> T,
            ): T = if (events.isEmpty()) afterEnqueue() else delegate.enqueueDataEventsThen(events, afterEnqueue)
        },
    )

    private val appContext = context.applicationContext
    private val legacyPreferences = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val encryptedPreferences = createEncryptedPreferences(appContext)
    private val sensitivePreferences = encryptedPreferences ?: legacyPreferences
    private val dataStore = dataStoreFor(appContext)
    private val database by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { IllustiaDatabase.getInstance(appContext) }
    private val dao by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { database.settingsDao() }

    init {
        migrateIfNeeded()
    }

    override suspend fun read(): AppSettings = readAppSettingsImpl(dataStore, sensitivePreferences, dao)

    override suspend fun readStartup(): AppSettings = readStartupAppSettingsImpl(dataStore, sensitivePreferences)

    override suspend fun write(
        settings: AppSettings,
        baseSettings: AppSettings?,
    ): AppSettings {
        val base = baseSettings ?: persistenceMutex.withLock { read() }
        val events =
            if (base.pallaSyncEnabled && settings.pallaSyncEnabled) {
                buildSettingsSyncEvents(base, settings)
            } else {
                emptyList()
            }

        suspend fun persistRebased(): AppSettings =
            persistenceMutex.withLock {
                val persisted = read()
                val rebasedCollections =
                    rebaseSyncedCollections(
                        base = base.syncedCollections(),
                        intended = settings.syncedCollections(),
                        persisted = persisted.syncedCollections(),
                    )
                val enabled =
                    if (base.pallaSyncEnabled == settings.pallaSyncEnabled) {
                        persisted.pallaSyncEnabled
                    } else {
                        settings.pallaSyncEnabled
                    }
                val serverUrl =
                    if (base.pallaSyncServerUrl == settings.pallaSyncServerUrl) {
                        persisted.pallaSyncServerUrl
                    } else {
                        settings.pallaSyncServerUrl
                    }
                val rebased =
                    settings
                        .copy(
                            pallaSyncEnabled = enabled,
                            pallaSyncServerUrl = serverUrl,
                        ).withSyncedCollections(rebasedCollections)
                writeAppSettingsImpl(dataStore, sensitivePreferences, database, dao, rebased, appContext.preferencesDataStoreFile(DATASTORE_NAME))
                legacyPreferences
                    .edit()
                    .putInt(KEY_IMAGE_CACHE_SIZE_MB, rebased.imageCacheSizeMb)
                    .putString(KEY_APP_LANGUAGE, rebased.appLanguage)
                    .putString(KEY_PERFORMANCE_MODE, rebased.performanceMode)
                    .putBoolean(KEY_STARTUP_PRIVACY_MODE, rebased.privacyModeEnabled)
                    .commit()

                rebased
            }

        return try {
            persistAfterSyncEnqueue(events, syncEventWriter) { persistRebased() }
        } catch (error: CancellationException) {
            throw error
        } catch (expectedFailure: Exception) {
            android.util.Log.e("AndroidSettingsStore", "Failed to durably enqueue local settings changes: ${expectedFailure.message}")
            throw expectedFailure
        }
    }

    override suspend fun writeSyncedCollections(synced: SyncedCollectionsSnapshot) {
        updateSyncedCollections { synced }
    }

    internal suspend fun readSyncedCollections(): SyncedCollectionsSnapshot = persistenceMutex.withLock { read().syncedCollections() }

    internal suspend fun updateSyncedCollections(
        transform: (SyncedCollectionsSnapshot) -> SyncedCollectionsSnapshot,
    ): SyncedCollectionsSnapshot {
        persistenceMutex.withLock {
            val current = read().syncedCollections()
            val updated = transform(current)
            if (updated != current) {
                writeSyncedCollectionsImpl(dataStore, database, dao, updated)
                publishSyncUpdate(updated)
            }
            return updated
        }
    }

    override fun setPallaSyncEnabledFromCoordinator(enabled: Boolean) {
        runBlocking {
            persistenceMutex.withLock {
                dataStore.edit { preferences -> preferences[PALLA_SYNC_ENABLED] = enabled }
                SettingsStore.notifyPallaSyncEnabledUpdate(
                    PallaSyncEnabledUpdate(
                        revision = pallaSyncStateRevision.incrementAndGet(),
                        enabled = enabled,
                    ),
                )
            }
        }
    }

    internal suspend fun setPallaSyncServerUrlFromCoordinator(serverUrl: String) {
        persistenceMutex.withLock {
            dataStore.edit { preferences -> preferences[PALLA_SYNC_SERVER_URL] = serverUrl }
        }
    }

    override suspend fun clearSensitive() {
        clearSensitiveSettingsImpl(dataStore, sensitivePreferences, legacyPreferences, database, dao)
    }

    override suspend fun getSavedIllusts(): List<SavedIllustItem> =
        withContext(Dispatchers.IO) {
            dao.getSavedIllusts().map {
                SavedIllustItem(
                    illustId = it.illustId,
                    title = it.title,
                    artistName = it.artistName,
                    artistId = it.artistId,
                    thumbUrl = it.thumbUrl,
                    localCoverPath = it.localCoverPath,
                    localPagePathsJson = it.localPagePathsJson,
                    pageCount = it.pageCount,
                    savedAt = it.savedAt,
                    saveGroup = it.saveGroup,
                    xRestrict = it.xRestrict,
                )
            }
        }

    override suspend fun getSavedIllust(illustId: Long): SavedIllustWithPages? =
        withContext(Dispatchers.IO) {
            val res = dao.getSavedIllust(illustId) ?: return@withContext null
            SavedIllustWithPages(
                illust = SavedIllustItem(
                    illustId = res.illust.illustId,
                    title = res.illust.title,
                    artistName = res.illust.artistName,
                    artistId = res.illust.artistId,
                    thumbUrl = res.illust.thumbUrl,
                    localCoverPath = res.illust.localCoverPath,
                    localPagePathsJson = res.illust.localPagePathsJson,
                    pageCount = res.illust.pageCount,
                    savedAt = res.illust.savedAt,
                    saveGroup = res.illust.saveGroup,
                    xRestrict = res.illust.xRestrict,
                ),
                pages = res.pages.map { it.localPath },
            )
        }

    override suspend fun insertSavedIllust(
        illust: SavedIllustItem,
        pages: List<String>,
    ) = withContext(Dispatchers.IO) {
        database.runInTransaction {
            dao.deleteSavedIllustPages(illust.illustId)
            dao.deleteSavedIllust(illust.illustId)
            val entity = SavedIllustEntity().apply {
                illustId = illust.illustId
                title = illust.title
                artistName = illust.artistName
                artistId = illust.artistId
                thumbUrl = illust.thumbUrl
                localCoverPath = illust.localCoverPath
                localPagePathsJson = illust.localPagePathsJson
                pageCount = illust.pageCount
                savedAt = illust.savedAt
                saveGroup = illust.saveGroup
                xRestrict = illust.xRestrict
            }
            val pageEntities = pages.mapIndexed { idx, path ->
                SavedIllustPageEntity().apply {
                    illustId = illust.illustId
                    pageIndex = idx
                    localPath = path
                    sourceUrl = ""
                }
            }
            dao.upsertSavedIllust(entity)
            dao.upsertSavedIllustPages(pageEntities)
        }
    }

    override suspend fun deleteSavedIllust(illustId: Long) =
        withContext(Dispatchers.IO) {
            database.runInTransaction {
                dao.deleteSavedIllustPages(illustId)
                dao.deleteSavedIllust(illustId)
            }
        }

    override fun savePinHash(pin: String) {
        savePinHashImpl(sensitivePreferences, pin)
    }

    override fun verifyPin(pin: String): Boolean = verifyPinHashImpl(sensitivePreferences, pin)

    override fun hasPinSet(): Boolean = hasPinSetImpl(sensitivePreferences)

    override fun clearPinHash() {
        clearPinHashImpl(sensitivePreferences)
    }

    override fun saveUnlockCodeHash(code: String) {
        saveUnlockCodeHashImpl(sensitivePreferences, code)
    }

    override fun verifyUnlockCode(code: String): Boolean = verifyUnlockCodeHashImpl(sensitivePreferences, code)

    override fun hasUnlockCodeSet(): Boolean = hasUnlockCodeSetImpl(sensitivePreferences)

    override fun clearUnlockCodeHash() {
        clearUnlockCodeHashImpl(sensitivePreferences)
    }

    override fun isValidUnlockCode(code: String): Boolean = isValidUnlockCodeImpl(code)

    override fun getSavedIllustStorageBytes(): Long =
        runBlocking(Dispatchers.IO) {
            savedIllustStorageBytesImpl(savedIllustDir())
        }

    override fun savedIllustDir(): File = File(appContext.filesDir, "saved_illusts")

    private fun migrateIfNeeded() {
        if (migrationCompleted) return
        synchronized(migrationLock) {
            if (migrationCompleted) return
            runBlocking(Dispatchers.IO) {
                migrateSettingsIfNeededImpl(
                    dataStore,
                    encryptedPreferences,
                    legacyPreferences,
                    database = { database },
                    dao = { dao },
                )
            }
            migrationCompleted = true
        }
    }

    companion object {
        private val syncRevision = AtomicLong(0L)
        private val pallaSyncStateRevision = AtomicLong(0L)
        private val persistenceMutex = Mutex()

        private fun publishSyncUpdate(collections: SyncedCollectionsSnapshot) {
            SettingsStore.notifySyncUpdate(
                SettingsSyncUpdate(
                    revision = syncRevision.incrementAndGet(),
                    collections = collections,
                ),
            )
        }

        @Volatile
        private var sharedDataStore: DataStore<Preferences>? = null

        @Volatile
        private var sharedEncryptedPreferences: SharedPreferences? = null

        @Volatile
        private var encryptedPreferencesInitialized = false

        @Volatile
        private var migrationCompleted = false
        private val encryptedPreferencesLock = Any()
        private val migrationLock = Any()

        fun resetForTesting() {
            synchronized(this) {
                sharedDataStore = null
                sharedEncryptedPreferences = null
                encryptedPreferencesInitialized = false
                migrationCompleted = false
            }
        }

        fun dataStoreFor(context: Context): DataStore<Preferences> =
            sharedDataStore ?: synchronized(this) {
                sharedDataStore ?: PreferenceDataStoreFactory
                    .createWithPath(
                        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO),
                        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME).absolutePath.toPath() },
                    ).also { sharedDataStore = it }
            }

        fun createEncryptedPreferences(context: Context): SharedPreferences? {
            if (encryptedPreferencesInitialized) return sharedEncryptedPreferences
            return synchronized(encryptedPreferencesLock) {
                if (!encryptedPreferencesInitialized) {
                    sharedEncryptedPreferences =
                        runCatching {
                            val appContext = context.applicationContext
                            val masterKey =
                                MasterKey
                                    .Builder(appContext)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                    .build()
                            EncryptedSharedPreferences.create(
                                appContext,
                                com.yunfie.illustia.settings.store.SECURE_PREFS_NAME,
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                            )
                        }.getOrNull()
                    encryptedPreferencesInitialized = sharedEncryptedPreferences != null
                }
                sharedEncryptedPreferences
            }
        }

        fun readStoredAppLanguage(context: Context): String {
            val appContext = context.applicationContext
            return appContext
                .getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_APP_LANGUAGE, "system")
                ?: "system"
        }

        fun isPrivacyModeEnabledSync(context: Context): Boolean {
            val appContext = context.applicationContext
            val startupPreferences =
                appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            if (startupPreferences.contains(KEY_STARTUP_PRIVACY_MODE)) {
                return startupPreferences.getBoolean(KEY_STARTUP_PRIVACY_MODE, false)
            }

            val enabled =
                runCatching {
                    appContext.packageManager.getComponentEnabledSetting(
                        ComponentName(appContext, DUMMY_LAUNCHER_ALIAS),
                    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                }.getOrDefault(false)
            startupPreferences
                .edit()
                .putBoolean(KEY_STARTUP_PRIVACY_MODE, enabled)
                .commit()
            return enabled
        }

        fun readImageCacheSizeMbSync(context: Context): Int =
            context.applicationContext
                .getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_IMAGE_CACHE_SIZE_MB, DEFAULT_IMAGE_CACHE_SIZE_MB)
                .coerceIn(MIN_IMAGE_CACHE_SIZE_MB, MAX_IMAGE_CACHE_SIZE_MB)

        fun readPerformanceModeSync(context: Context): String =
            context.applicationContext
                .getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PERFORMANCE_MODE, "auto")
                ?.takeIf { it in setOf("auto", "lightweight", "quality") }
                ?: "auto"

        private const val KEY_IMAGE_CACHE_SIZE_MB = "startup_image_cache_size_mb"
        private const val KEY_STARTUP_PRIVACY_MODE = "startup_privacy_mode_enabled"
        private const val DUMMY_LAUNCHER_ALIAS = "com.yunfie.illustia.MainActivityDummy"
        private const val DEFAULT_IMAGE_CACHE_SIZE_MB = 300
        private const val MIN_IMAGE_CACHE_SIZE_MB = 100
        private const val MAX_IMAGE_CACHE_SIZE_MB = 1000
    }
}

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.TIRAMISU)
fun appLanguageLocaleList(language: String): android.os.LocaleList =
    android.os.LocaleList.forLanguageTags(
        when (language) {
            "ja" -> "ja-JP"
            "en" -> "en-US"
            "ko" -> "ko-KR"
            "es" -> "es-ES"
            "pt" -> "pt-BR"
            "fr" -> "fr-FR"
            "de" -> "de-DE"
            "ru" -> "ru-RU"
            "id" -> "id-ID"
            "th" -> "th-TH"
            "vi" -> "vi-VN"
            "zh-Hans" -> "zh-Hans"
            "zh-Hant" -> "zh-Hant"
            else -> ""
        },
    )

