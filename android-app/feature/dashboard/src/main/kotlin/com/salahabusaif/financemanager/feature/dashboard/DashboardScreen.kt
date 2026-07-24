package com.salahabusaif.financemanager.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesRepository
import com.salahabusaif.financemanager.core.designsystem.BalanceHeroCard
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.MoneySummaryCard
import com.salahabusaif.financemanager.core.designsystem.MoneyText
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.model.CurrencyCode
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.money.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(private val preferencesRepository: AppPreferencesRepository, ledgerGateway: LedgerGateway) : ViewModel() {
    val preferences = preferencesRepository.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.salahabusaif.financemanager.core.model.AppPreferences())
    val accounts = ledgerGateway.financialAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun toggleAmounts() = viewModelScope.launch { preferencesRepository.setHideAmounts(!preferences.value.hideAmounts) }
}

@Composable
fun DashboardScreen(
    onOpenAccounts: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val balances = CurrencyCode.entries.associateWith { currency ->
        accounts.filter { !it.isArchived && it.currency == currency }.sumOf { it.balanceMinor }
    }
    var selectedCurrency by rememberSaveable { mutableStateOf(CurrencyCode.ILS) }
    val visibleBalances = DashboardBalances.visible(balances)
    Column(
        Modifier.fillMaxSize().padding(FinanceSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
    ) {
        if (visibleBalances.isNotEmpty()) {
            BalanceHeroCard(
                balances = balances,
                selectedCurrency = selectedCurrency,
                hidden = preferences.hideAmounts,
                onToggleVisibility = viewModel::toggleAmounts,
                onCurrencySelected = { selectedCurrency = it },
            )
            CurrencyTotalsCard(balances, preferences.hideAmounts)
        } else {
            EmptyState(stringResource(R.string.no_balances_recorded), "", Icons.AutoMirrored.Filled.ReceiptLong)
        }
        Button(onClick = onOpenAccounts, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.my_accounts_action))
        }
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

@Composable
private fun CurrencyTotalsCard(balances: Map<CurrencyCode, Long>, hidden: Boolean) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
        Text(stringResource(R.string.total_assets), style = MaterialTheme.typography.titleMedium)
        DashboardBalances.visible(balances).forEach { (currency, balance) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currencyLabel(currency), color = MaterialTheme.colorScheme.onSurfaceVariant)
                com.salahabusaif.financemanager.core.designsystem.HiddenMoneyText(Money(balance, currency), hidden)
            }
        }
    }
}

@Composable
private fun currencyLabel(currency: CurrencyCode): String = stringResource(
    when (currency) {
        CurrencyCode.ILS -> R.string.currency_ils
        CurrencyCode.USD -> R.string.currency_usd
        CurrencyCode.JOD -> R.string.currency_jod
    },
)
