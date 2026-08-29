package com.yunfie.illustia

import com.yunfie.illustia.data.IllustiaRepository
import com.yunfie.illustia.platform.PlatformActions
import com.yunfie.illustia.settings.SettingsStore

/**
 * Stable implementation entry point assembled from responsibility-focused modules.
 *
 * Keep this class intentionally small: new behavior belongs in the closest feature
 * module, while state shared across modules belongs in [IllustiaViewModelFoundation].
 */
open class IllustiaViewModelCore(
    settingsStore: SettingsStore,
    repository: IllustiaRepository = IllustiaRepository(settingsStore),
    platformActions: PlatformActions? = null,
) : IllustiaLibraryNavigationModule(settingsStore, repository, platformActions)
