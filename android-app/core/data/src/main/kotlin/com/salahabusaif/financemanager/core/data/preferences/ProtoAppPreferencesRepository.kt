package com.salahabusaif.financemanager.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.DataStoreFactory
import android.content.Context
import com.salahabusaif.financemanager.core.data.proto.StoredAppPreferences
import com.salahabusaif.financemanager.core.model.AppLanguage
import com.salahabusaif.financemanager.core.model.AppPreferences
import com.salahabusaif.financemanager.core.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ProtoAppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<StoredAppPreferences>,
) : AppPreferencesRepository {
    override val preferences: Flow<AppPreferences> = dataStore.data.map { stored ->
        AppPreferences(
            language = if (stored.language == StoredAppPreferences.Language.ENGLISH) AppLanguage.ENGLISH else AppLanguage.ARABIC,
            theme = when (stored.theme) {
                StoredAppPreferences.Theme.LIGHT -> AppTheme.LIGHT
                StoredAppPreferences.Theme.DARK -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            },
            hideAmounts = stored.hideAmounts,
        )
    }

    override suspend fun setLanguage(language: AppLanguage) = update { builder ->
        builder.language = if (language == AppLanguage.ENGLISH) StoredAppPreferences.Language.ENGLISH else StoredAppPreferences.Language.ARABIC
    }

    override suspend fun setTheme(theme: AppTheme) = update { builder ->
        builder.theme = when (theme) {
            AppTheme.SYSTEM -> StoredAppPreferences.Theme.SYSTEM
            AppTheme.LIGHT -> StoredAppPreferences.Theme.LIGHT
            AppTheme.DARK -> StoredAppPreferences.Theme.DARK
        }
    }

    override suspend fun setHideAmounts(hidden: Boolean) = update { builder -> builder.hideAmounts = hidden }

    private suspend fun update(change: (StoredAppPreferences.Builder) -> Unit) {
        dataStore.updateData { current -> current.toBuilder().apply(change).build() }
    }
}
