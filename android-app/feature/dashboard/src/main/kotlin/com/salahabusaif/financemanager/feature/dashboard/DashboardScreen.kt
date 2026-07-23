package com.salahabusaif.financemanager.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesRepository
import com.salahabusaif.financemanager.core.designsystem.BalanceHeroCard
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.MoneySummaryCard
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.model.CurrencyCode
import com.salahabusaif.financemanager.core.money.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(private val preferencesRepository: AppPreferencesRepository) : ViewModel() {
    val preferences = preferencesRepository.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.salahabusaif.financemanager.core.model.AppPreferences())
    fun toggleAmounts() = viewModelScope.launch { preferencesRepository.setHideAmounts(!preferences.value.hideAmounts) }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsState()
    Column(
        Modifier.fillMaxSize().padding(FinanceSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
    ) {
        BalanceHeroCard(preferences.hideAmounts, viewModel::toggleAmounts)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            MoneySummaryCard(
                stringResource(R.string.income),
                Money(0, CurrencyCode.ILS),
                preferences.hideAmounts,
                Icons.AutoMirrored.Filled.TrendingUp,
                Modifier.weight(1f),
            )
            MoneySummaryCard(
                stringResource(R.string.expenses),
                Money(0, CurrencyCode.ILS),
                preferences.hideAmounts,
                Icons.AutoMirrored.Filled.TrendingDown,
                Modifier.weight(1f),
            )
        }
        Text(
            stringResource(R.string.no_review_items),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        EmptyState(
            stringResource(R.string.no_transactions),
            stringResource(R.string.no_transactions_detail),
            Icons.AutoMirrored.Filled.ReceiptLong,
        )
    }
}
