package com.yunfie.illustia

import com.yunfie.illustia.data.IllustiaRepository
import com.yunfie.illustia.platform.PlatformActions
import com.yunfie.illustia.settings.SettingsStore

/**
 * Stable ViewModel entry point used by the UI and dependency injection.
 *
 * Feature implementation lives under the `viewmodel` source folder so this
 * public type can remain source-compatible while its responsibilities evolve.
 */
open class IllustiaViewModel(
    settingsStore: SettingsStore,
    repository: IllustiaRepository = IllustiaRepository(settingsStore),
    platformActions: PlatformActions? = null,
) : IllustiaViewModelCore(settingsStore, repository, platformActions)
