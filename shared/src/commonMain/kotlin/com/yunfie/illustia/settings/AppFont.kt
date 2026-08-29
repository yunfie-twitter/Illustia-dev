package com.yunfie.illustia.settings

import com.yunfie.illustia.*
import org.jetbrains.compose.resources.StringResource

enum class AppFont(
    val value: String,
) {
    System("system"),
    MiSans("misans"),
    ;

    companion object {
        fun fromValue(value: String): AppFont = entries.firstOrNull { it.value == value } ?: System
    }
}

fun appFontOptions(): List<String> = AppFont.entries.map { it.value }

fun appFontLabelRes(value: String): StringResource =
    when (AppFont.fromValue(value)) {
        AppFont.System -> R.string.font_system
        AppFont.MiSans -> R.string.font_misans
    }
