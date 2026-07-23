package com.salahabusaif.financemanager.core.data.preferences

import com.salahabusaif.financemanager.core.model.AppLanguage
import com.salahabusaif.financemanager.core.model.AppPreferences
import com.salahabusaif.financemanager.core.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val preferences: Flow<AppPreferences>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setHideAmounts(hidden: Boolean)
}
