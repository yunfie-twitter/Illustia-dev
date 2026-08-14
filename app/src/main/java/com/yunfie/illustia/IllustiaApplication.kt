package com.yunfie.illustia

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.yunfie.illustia.account.PalleriaAccount
import com.yunfie.illustia.data.IllustiaRepository
import com.yunfie.illustia.pallasync.PalleriaSyncCoordinator
import com.yunfie.illustia.performance.DevicePerformance
import com.yunfie.illustia.platform.PlatformCapabilities
import com.yunfie.illustia.settings.SettingsStore
import com.yunfie.illustia.widget.IllustWidgetProvider
import com.yunfie.illustia.widget.RankingWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class IllustiaApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val postStartupWorkStarted = AtomicBoolean(false)
    @Volatile private var appImageLoader: ImageLoader? = null
    @Volatile private var activeDecodeExecutor: ThreadPoolExecutor? = null
    @Volatile private var activeNetworkDispatcher: Dispatcher? = null

    val settingsStore: SettingsStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SettingsStore(this)
    }

    val repository: IllustiaRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        IllustiaRepository(settingsStore)
    }

    val sharedHttpClient: OkHttpClient by lazy {
        val performance = DevicePerformance.profile
        val networkDispatcher =
            Dispatcher().apply {
                maxRequests = performance.maxNetworkRequests
                maxRequestsPerHost = performance.maxNetworkRequestsPerHost
            }
        activeNetworkDispatcher = networkDispatcher
        OkHttpClient
            .Builder()
            .dispatcher(networkDispatcher)
            .connectionPool(okhttp3.ConnectionPool(performance.maxNetworkRequestsPerHost, 5, TimeUnit.MINUTES))
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** The only stateful PallaSync coordinator in this application process. */
    internal val pallaSyncCoordinator: PalleriaSyncCoordinator by lazy {
        PalleriaSyncCoordinator(
            client = sharedHttpClient,
            context = this,
            coordinatorScope = appScope,
        )
    }

    override fun onCreate() {
        super.onCreate()
        val performance = DevicePerformance.initialize(this)
        CrashHandler.instance.init(this)
        val appContext = applicationContext
        val cacheDirectory = cacheDir.resolve("image_cache").toOkioPath()
        val configuredCacheMb = SettingsStore.readImageCacheSizeMbSync(appContext)
        SingletonImageLoader.setSafe {
            val initialDecodeParallelism = DevicePerformance.runtimePolicy.value.imageDecodeParallelism
            val decodeExecutor =
                ThreadPoolExecutor(
                    initialDecodeParallelism,
                    initialDecodeParallelism,
                    30L,
                    TimeUnit.SECONDS,
                    LinkedBlockingQueue(),
                ).apply { allowCoreThreadTimeOut(true) }
            activeDecodeExecutor = decodeExecutor
            ImageLoader
                .Builder(appContext)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = { sharedHttpClient }))
                    if (PlatformCapabilities.supportsAnimatedImageDecoder()) {
                        add(AnimatedImageDecoder.Factory())
                    }
                    add(GifDecoder.Factory())
                }.memoryCache {
                    MemoryCache
                        .Builder()
                        .maxSizePercent(appContext, performance.imageMemoryCachePercent)
                        .build()
                }.diskCache {
                    DiskCache
                        .Builder()
                        .directory(cacheDirectory)
                        .maxSizeBytes(configuredCacheMb.toLong() * 1024 * 1024)
                        .build()
                }.fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(performance.imageFetchParallelism))
                .decoderCoroutineContext(decodeExecutor.asCoroutineDispatcher())
                .build()
                .also { appImageLoader = it }
        }
        appScope.launch {
            DevicePerformance.runtimePolicy.collect { policy ->
                resizeDecodePool(policy.imageDecodeParallelism)
                activeNetworkDispatcher?.let { dispatcher ->
                    dispatcher.maxRequests = policy.networkRequestParallelism
                    dispatcher.maxRequestsPerHost =
                        minOf(DevicePerformance.profile.maxNetworkRequestsPerHost, policy.networkRequestParallelism)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val isMemoryPressure =
            level == TRIM_MEMORY_RUNNING_LOW ||
                level == TRIM_MEMORY_RUNNING_CRITICAL ||
                level == TRIM_MEMORY_BACKGROUND ||
                level == TRIM_MEMORY_MODERATE ||
                level == TRIM_MEMORY_COMPLETE
        if (!isMemoryPressure) return

        DevicePerformance.onMemoryPressure()
        appImageLoader?.memoryCache?.let { cache ->
            if (level == TRIM_MEMORY_RUNNING_CRITICAL || level == TRIM_MEMORY_COMPLETE) {
                cache.clear()
            } else {
                cache.trimToSize(cache.maxSize / 2L)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        DevicePerformance.onMemoryPressure()
        appImageLoader?.memoryCache?.clear()
    }

    private fun resizeDecodePool(parallelism: Int) {
        val executor = activeDecodeExecutor ?: return
        val target = parallelism.coerceAtLeast(1)
        synchronized(executor) {
            if (target < executor.corePoolSize) {
                executor.corePoolSize = target
                executor.maximumPoolSize = target
            } else {
                executor.maximumPoolSize = target
                executor.corePoolSize = target
            }
        }
    }

    /** Starts non-critical process maintenance after the first app frame is available. */
    fun startPostStartupWork() {
        if (!postStartupWorkStarted.compareAndSet(false, true)) return

        appScope.launch {
            delay(DevicePerformance.profile.postStartupWorkDelayMs)
            val appContext = applicationContext
            val recoveredPallaSync =
                runCatching {
                    pallaSyncCoordinator.recoverInterruptedActivation()
                }.getOrDefault(false)
            val settings = repository.readSettings()
            PalleriaAccount.reconcile(appContext, settings.accounts)
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

    fun setTelemetryEnabled(enabled: Boolean) {
        GlitchTipTelemetry.setEnabled(applicationContext, enabled)
    }
}
