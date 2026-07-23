package com.salahabusaif.financemanager.core.model

enum class AppLanguage(val languageTag: String) {
    ARABIC("ar"),
    ENGLISH("en"),
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppPreferences(
    val language: AppLanguage = AppLanguage.ARABIC,
    val theme: AppTheme = AppTheme.SYSTEM,
    val hideAmounts: Boolean = false,
)
