package com.yunfie.illustia

import android.content.Context
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

/** Controls the GlitchTip-compatible Sentry SDK without bypassing telemetry consent. */
object GlitchTipTelemetry {
    private val lock = Any()

    @Volatile
    private var enabled = false

    fun setEnabled(
        context: Context,
        shouldEnable: Boolean,
    ) {
        synchronized(lock) {
            if (enabled == shouldEnable) return

            if (shouldEnable) {
                SentryAndroid.init(context.applicationContext) { options ->
                    // GlitchTip does not support Sentry session tracking.
                    options.isEnableAutoSessionTracking = false
                    options.isSendDefaultPii = false
                }
                enabled = Sentry.isEnabled()
            } else {
                Sentry.close()
                enabled = false
            }
        }
    }
}
