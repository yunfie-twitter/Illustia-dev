package com.yunfie.illustia.settings

import com.yunfie.illustia.R
import org.jetbrains.compose.resources.StringResource
import java.util.Locale

enum class AppLanguage(
    val value: String,
    val languageTag: String?,
) {
    System("system", null),
    Japanese("ja", "ja-JP"),
    English("en", "en-US"),
    Korean("ko", "ko-KR"),
    Spanish("es", "es-ES"),
    Portuguese("pt", "pt-BR"),
    French("fr", "fr-FR"),
    German("de", "de-DE"),
    Russian("ru", "ru-RU"),
    Indonesian("id", "id-ID"),
    Thai("th", "th-TH"),
    Vietnamese("vi", "vi-VN"),
    SimplifiedChinese("zh-Hans", "zh-Hans"),
    TraditionalChinese("zh-Hant", "zh-Hant"),
    ;

    companion object {
        fun fromValue(value: String): AppLanguage = entries.firstOrNull { it.value == value } ?: System
    }
}

fun appLanguageOptions(): List<String> = AppLanguage.entries.map { it.value }

fun appLanguageLabelRes(value: String): StringResource =
    when (AppLanguage.fromValue(value)) {
        AppLanguage.System -> R.string.language_system
        AppLanguage.Japanese -> R.string.language_japanese
        AppLanguage.English -> R.string.language_english
        AppLanguage.Korean -> R.string.language_korean
        AppLanguage.Spanish -> R.string.language_spanish
        AppLanguage.Portuguese -> R.string.language_portuguese
        AppLanguage.French -> R.string.language_french
        AppLanguage.German -> R.string.language_german
        AppLanguage.Russian -> R.string.language_russian
        AppLanguage.Indonesian -> R.string.language_indonesian
        AppLanguage.Thai -> R.string.language_thai
        AppLanguage.Vietnamese -> R.string.language_vietnamese
        AppLanguage.SimplifiedChinese -> R.string.language_chinese_simplified
        AppLanguage.TraditionalChinese -> R.string.language_chinese_traditional
    }

fun currentAcceptLanguage(): String {
    val locale = Locale.getDefault()
    val primaryLanguage = locale.language.orEmpty()
    return when (primaryLanguage) {
        "ja" -> AppLanguage.Japanese.languageTag ?: "ja-JP"
        "ko" -> AppLanguage.Korean.languageTag ?: "ko-KR"
        "es" -> AppLanguage.Spanish.languageTag ?: "es-ES"
        "pt" -> AppLanguage.Portuguese.languageTag ?: "pt-BR"
        "fr" -> AppLanguage.French.languageTag ?: "fr-FR"
        "de" -> AppLanguage.German.languageTag ?: "de-DE"
        "ru" -> AppLanguage.Russian.languageTag ?: "ru-RU"
        "id" -> AppLanguage.Indonesian.languageTag ?: "id-ID"
        "th" -> AppLanguage.Thai.languageTag ?: "th-TH"
        "vi" -> AppLanguage.Vietnamese.languageTag ?: "vi-VN"
        "zh" -> {
            if (locale.script.equals("Hant", ignoreCase = true) ||
                locale.country in setOf("TW", "HK", "MO")
            ) {
                AppLanguage.TraditionalChinese.languageTag ?: "zh-Hant"
            } else {
                AppLanguage.SimplifiedChinese.languageTag ?: "zh-Hans"
            }
        }
        else -> AppLanguage.English.languageTag ?: "en-US"
    }
}

