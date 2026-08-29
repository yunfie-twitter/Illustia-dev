package com.yunfie.illustia.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.yunfie.illustia.settings.AppHapticMode

val LocalAppHapticMode = compositionLocalOf { AppHapticMode.Rich }

val NoOpHapticFeedback =
    object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
    }

enum class AppHapticEffect {
    Click,
    Toggle,
    Success,
    Error,
}

fun performAppHapticFeedback(
    context: Any? = null,
    hapticFeedback: HapticFeedback,
    mode: AppHapticMode,
    effect: AppHapticEffect = AppHapticEffect.Click,
) {
    if (mode == AppHapticMode.Off) return
    runCatching {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}

fun isAppHapticsSupported(context: Any? = null): Boolean = true
