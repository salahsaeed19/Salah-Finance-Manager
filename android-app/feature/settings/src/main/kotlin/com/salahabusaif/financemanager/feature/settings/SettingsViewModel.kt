package com.salahabusaif.financemanager.feature.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesRepository
import com.salahabusaif.financemanager.core.data.profile.OwnerProfile
import com.salahabusaif.financemanager.core.data.profile.OwnerProfileRepository
import com.salahabusaif.financemanager.core.model.AppLanguage
import com.salahabusaif.financemanager.core.model.AppPreferences
import com.salahabusaif.financemanager.core.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppPreferencesRepository,
    private val ownerProfileRepository: OwnerProfileRepository,
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {
    val preferences = repository.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())
    val ownerProfile = ownerProfileRepository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OwnerProfile())

    init {
        viewModelScope.launch { ownerProfileRepository.bootstrapIfAbsent() }
    }

    fun setLanguage(language: AppLanguage) {
        applyApplicationLocale(language)
        viewModelScope.launch {
            withContext(NonCancellable) {
                repository.setLanguage(language)
            }
        }
    }

    fun setTheme(theme: AppTheme) = viewModelScope.launch { repository.setTheme(theme) }
    fun setHideAmounts(hidden: Boolean) = viewModelScope.launch { repository.setHideAmounts(hidden) }

    fun updateOwnerProfile(profile: OwnerProfile) = viewModelScope.launch { ownerProfileRepository.update(profile) }

    private fun applyApplicationLocale(language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(language.languageTag)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
        }
    }
}
