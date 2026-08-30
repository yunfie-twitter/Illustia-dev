package com.yunfie.illustia.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.yunfie.illustia.settings.store.KEY_PERFORMANCE_MODE
import com.yunfie.illustia.settings.store.LEGACY_PREFS_NAME
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class SettingsStoreStartupTest {
    @Test
    fun `performance mode is available from the startup mirror`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val original = preferences.getString(KEY_PERFORMANCE_MODE, null)

        try {
            preferences.edit().putString(KEY_PERFORMANCE_MODE, "lightweight").commit()

            AndroidSettingsStore.readPerformanceModeSync(context) shouldBe "lightweight"
        } finally {
            val editor = preferences.edit()
            if (original == null) editor.remove(KEY_PERFORMANCE_MODE) else editor.putString(KEY_PERFORMANCE_MODE, original)
            editor.commit()
        }
    }

    @Test
    fun `startup read skips Room collections and uses the privacy mirror`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val store = AndroidSettingsStore(context)
            val original = store.read()
            val marker = "startup-room-marker"

            try {
                val updated =
                    original.copy(
                        privacyModeEnabled = true,
                        searchHistory = listOf(marker),
                        favoriteTags = listOf(marker),
                    )
                store.write(updated, original)

                AndroidSettingsStore.isPrivacyModeEnabledSync(context).shouldBeTrue()
                store.readStartup().apply {
                    privacyModeEnabled.shouldBeTrue()
                    searchHistory.shouldBeEmpty()
                    favoriteTags.shouldBeEmpty()
                }
                store.read().apply {
                    searchHistory.shouldContain(marker)
                    favoriteTags.shouldContain(marker)
                }
            } finally {
                store.write(original, store.read())
            }
        }
    }
}
