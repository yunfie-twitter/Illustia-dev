package com.yunfie.illustia.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.yunfie.illustia.settings.AppHapticMode

val LocalAppHapticMode = compositionLocalOf { AppHapticMode.Rich }

enum class AppHapticEffect {
    Click,
    Toggle,
    Success,
    Error,
}

fun performAppHapticFeedback(
    context: Context,
    hapticFeedback: HapticFeedback,
    mode: AppHapticMode,
    effect: AppHapticEffect = AppHapticEffect.Click,
) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator == null || !vibrator.hasVibrator()) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        return
    }

    val vibrationPlayed = runCatching {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                mode == AppHapticMode.Rich &&
                vibrator.vibrateCompositionIfSupported(effect) -> Unit
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                vibrator.vibrate(VibrationEffect.createPredefined(effect.predefinedEffect(mode)))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                vibrator.vibrate(effect.compatEffect(mode, vibrator.hasAmplitudeControl()))
            }
            else -> {
                @Suppress("DEPRECATION")
                when (effect) {
                    AppHapticEffect.Success -> vibrator.vibrate(longArrayOf(0L, 18L, 30L, 28L), -1)
                    AppHapticEffect.Error -> vibrator.vibrate(longArrayOf(0L, 30L, 32L, 42L), -1)
                    AppHapticEffect.Click,
                    AppHapticEffect.Toggle -> vibrator.vibrate(if (mode == AppHapticMode.Rich) 28L else 16L)
                }
            }
        }
    }.isSuccess

    if (!vibrationPlayed) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun Vibrator.vibrateCompositionIfSupported(effect: AppHapticEffect): Boolean {
    val primitivesSupported = when (effect) {
        AppHapticEffect.Click -> areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
        AppHapticEffect.Toggle -> areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)
        AppHapticEffect.Success -> areAllPrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
            VibrationEffect.Composition.PRIMITIVE_CLICK,
        )
        AppHapticEffect.Error -> areAllPrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_QUICK_FALL,
            VibrationEffect.Composition.PRIMITIVE_CLICK,
        )
    }
    if (!primitivesSupported) return false

    val composition = VibrationEffect.startComposition()
    when (effect) {
        AppHapticEffect.Click -> composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.85f)
        AppHapticEffect.Toggle -> composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.75f)
        AppHapticEffect.Success -> composition
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.45f)
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.95f, 20)
        AppHapticEffect.Error -> composition
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.7f)
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f, 25)
    }
    vibrate(composition.compose())
    return true
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun AppHapticEffect.predefinedEffect(mode: AppHapticMode): Int {
    return when (this) {
        AppHapticEffect.Click -> if (mode == AppHapticMode.Rich) {
            VibrationEffect.EFFECT_HEAVY_CLICK
        } else {
            VibrationEffect.EFFECT_CLICK
        }
        AppHapticEffect.Toggle -> VibrationEffect.EFFECT_TICK
        AppHapticEffect.Success -> VibrationEffect.EFFECT_HEAVY_CLICK
        AppHapticEffect.Error -> VibrationEffect.EFFECT_DOUBLE_CLICK
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun AppHapticEffect.compatEffect(
    mode: AppHapticMode,
    hasAmplitudeControl: Boolean,
): VibrationEffect {
    val strongAmplitude = if (hasAmplitudeControl) 220 else VibrationEffect.DEFAULT_AMPLITUDE
    val lightAmplitude = if (hasAmplitudeControl) 120 else VibrationEffect.DEFAULT_AMPLITUDE
    return when (this) {
        AppHapticEffect.Success -> if (hasAmplitudeControl) {
            VibrationEffect.createWaveform(
                longArrayOf(0L, 14L, 24L, 28L),
                intArrayOf(0, lightAmplitude, 0, strongAmplitude),
                -1,
            )
        } else {
            VibrationEffect.createWaveform(longArrayOf(0L, 14L, 24L, 28L), -1)
        }
        AppHapticEffect.Error -> if (hasAmplitudeControl) {
            VibrationEffect.createWaveform(
                longArrayOf(0L, 28L, 30L, 40L),
                intArrayOf(0, strongAmplitude, 0, strongAmplitude),
                -1,
            )
        } else {
            VibrationEffect.createWaveform(longArrayOf(0L, 28L, 30L, 40L), -1)
        }
        AppHapticEffect.Click,
        AppHapticEffect.Toggle -> VibrationEffect.createOneShot(
            if (mode == AppHapticMode.Rich) 24L else 14L,
            if (mode == AppHapticMode.Rich) strongAmplitude else lightAmplitude,
        )
    }
}
