package com.yunfie.illustia

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.yunfie.illustia.widget.IllustWidgetProvider
import com.yunfie.illustia.widget.RankingWidgetProvider
import kotlinx.coroutines.CoroutineScope
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.yunfie.illustia.settings.SettingsStore
import com.yunfie.illustia.data.IllustiaRepository
import com.yunfie.illustia.account.PalleriaAccount
import com.yunfie.illustia.pallasync.PalleriaSyncManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class IllustiaApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val postStartupWorkStarted = AtomicBoolean(false)

    val settingsStore: SettingsStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SettingsStore(this)
    }

    val repository: IllustiaRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        IllustiaRepository(settingsStore)
    }

    val sharedHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().apply {
                    maxRequests = 8
                    maxRequestsPerHost = 4
                }
            )
            .connectionPool(okhttp3.ConnectionPool(4, 5, TimeUnit.MINUTES))
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** The only stateful PallaSync coordinator in this application process. */
    val pallaSyncCoordinator: PalleriaSyncManager by lazy {
        PalleriaSyncManager(
            client = sharedHttpClient,
            context = this,
            coordinatorScope = appScope,
        )
    }

    override fun onCreate() {
        super.onCreate()
        CrashHandler.instance.init(this)
        val appContext = applicationContext
        val cacheDirectory = cacheDir.resolve("image_cache").toOkioPath()
        val configuredCacheMb = SettingsStore.readImageCacheSizeMbSync(appContext)
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(appContext)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = { sharedHttpClient }))
                    add(AnimatedImageDecoder.Factory())
                    add(GifDecoder.Factory())
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(appContext, 0.06)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDirectory)
                        .maxSizeBytes(configuredCacheMb.toLong() * 1024 * 1024)
                        .build()
                }
                .build()
        }
    }

    /** Starts non-critical process maintenance after the first app frame is available. */
    fun startPostStartupWork() {
        if (!postStartupWorkStarted.compareAndSet(false, true)) return

        appScope.launch {
            val appContext = applicationContext
            val recoveredPallaSync = runCatching {
                pallaSyncCoordinator.recoverInterruptedActivation()
            }.getOrDefault(false)
            val settings = repository.readSettings()
            PalleriaAccount.reconcile(appContext, settings.accounts)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(settings.sendTelemetry)
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = settings.sendTelemetry
            RankingWidgetProvider.publishPreview(appContext)
            IllustWidgetProvider.publishPreview(appContext)
            setPallaSyncEnabled(recoveredPallaSync || settings.pallaSyncEnabled)
        }
    }

    fun setPallaSyncEnabled(enabled: Boolean) {
        if (enabled) {
            pallaSyncCoordinator.startBackgroundSync()
        } else {
            pallaSyncCoordinator.stopBackgroundSync()
        }
    }
}
