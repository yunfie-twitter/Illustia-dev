package com.yunfie.illustia

// SettingsComponents.kt
@Composable
fun SettingLinkRow(title: String, summary: String? = null, onClick: () -> Unit) {
    ArrowPreference(
        title = title,
        summary = summary ?: "",
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    )
}