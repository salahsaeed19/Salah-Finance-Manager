package com.salahabusaif.financemanager.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.MoneyText
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.ledger.FinancialAccount
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.ledger.LedgerTransactionSummary
import com.salahabusaif.financemanager.core.ledger.LedgerTransactionType
import com.salahabusaif.financemanager.core.ledger.RecordTransactionCommand
import com.salahabusaif.financemanager.core.ledger.TransferCommand
import com.salahabusaif.financemanager.core.ledger.ExchangeCommand
import com.salahabusaif.financemanager.core.money.Money
import java.math.BigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionsViewModel @Inject constructor(private val ledgerGateway: LedgerGateway) : ViewModel() {
    val accounts = ledgerGateway.financialAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = ledgerGateway.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun record(account: FinancialAccount, amount: Long, description: String, income: Boolean) = viewModelScope.launch {
        ledgerGateway.recordPersonalTransaction(RecordTransactionCommand(UUID.randomUUID().toString(), account.id, amount, description, if (income) LedgerTransactionType.OTHER_INCOME else LedgerTransactionType.PERSONAL_EXPENSE))
    }

    fun reverse(transaction: LedgerTransactionSummary) = viewModelScope.launch { ledgerGateway.reverse(transaction.id, UUID.randomUUID().toString()) }

    fun transfer(source: FinancialAccount, destination: FinancialAccount, amount: Long, description: String) = viewModelScope.launch {
        ledgerGateway.recordTransfer(TransferCommand(UUID.randomUUID().toString(), source.id, destination.id, amount, description))
    }

    fun exchange(source: FinancialAccount, destination: FinancialAccount, sourceAmount: Long, destinationAmount: Long, description: String) = viewModelScope.launch {
        ledgerGateway.recordExchange(ExchangeCommand(UUID.randomUUID().toString(), source.id, destination.id, sourceAmount, destinationAmount, description))
    }
}

@Composable
fun TransactionsScreen(
    onCreateAccount: () -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var income by remember { mutableStateOf<Boolean?>(null) }
    var transfer by remember { mutableStateOf(false) }
    var exchange by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md)) {
        if (accounts.isEmpty()) {
            EmptyState(stringResource(R.string.no_transactions), stringResource(R.string.no_account_explanation), Icons.AutoMirrored.Filled.ReceiptLong)
            Button(onClick = onCreateAccount, modifier = Modifier.fillMaxWidth().testTag("create_first_account")) { Text(stringResource(R.string.create_first_account)) }
        } else {
            Button(onClick = { income = true }, modifier = Modifier.fillMaxWidth().testTag("add_income")) { Text(stringResource(R.string.add_income)) }
            Button(onClick = { income = false }, modifier = Modifier.fillMaxWidth().testTag("add_expense")) { Text(stringResource(R.string.add_expense)) }
            Button(onClick = { transfer = true }, modifier = Modifier.fillMaxWidth(), enabled = accounts.size > 1) { Text(stringResource(R.string.transfer_transaction)) }
            Button(onClick = { exchange = true }, modifier = Modifier.fillMaxWidth(), enabled = accounts.any { first -> accounts.any { second -> first.currency != second.currency } }) { Text(stringResource(R.string.exchange_transaction)) }
        }
        if (transactions.isEmpty() && accounts.isNotEmpty()) EmptyState(stringResource(R.string.no_transactions), stringResource(R.string.no_transactions_detail), Icons.AutoMirrored.Filled.ReceiptLong)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) { items(transactions, key = LedgerTransactionSummary::id) { transaction -> TransactionCard(transaction) { viewModel.reverse(transaction) } } }
    }
    income?.let { isIncome -> RecordDialog(accounts.firstOrNull(), isIncome, { income = null }) { amount, description ->
        accounts.firstOrNull()?.let { viewModel.record(it, amount, description, isIncome) }
        income = null
    } }
    if (transfer) RecordDialog(accounts.firstOrNull(), false, { transfer = false }) { amount, description ->
        if (accounts.size > 1) viewModel.transfer(accounts.first(), accounts[1], amount, description)
        transfer = false
    }
    if (exchange) ExchangeDialog(accounts, onDismiss = { exchange = false }) { source, destination, sourceAmount, destinationAmount, description ->
        viewModel.exchange(source, destination, sourceAmount, destinationAmount, description)
        exchange = false
    }
}

@Composable
private fun TransactionCard(transaction: LedgerTransactionSummary, onReverse: () -> Unit) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.xs)) {
        Text(transaction.description, style = MaterialTheme.typography.titleMedium)
        MoneyText(Money(transaction.amountMinor, transaction.currency))
        if (transaction.isReversed) Text(stringResource(R.string.reversed), color = MaterialTheme.colorScheme.error)
        else TextButton(onClick = onReverse) { Text(stringResource(R.string.reverse_transaction)) }
    }
}

@Composable
private fun RecordDialog(account: FinancialAccount?, income: Boolean, onDismiss: () -> Unit, onRecord: (Long, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(if (income) R.string.income_transaction else R.string.expense_transaction)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            Text(account?.name ?: stringResource(R.string.no_eligible_accounts))
            OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.amount)) }, singleLine = true)
            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.description)) }, singleLine = true)
        }
    }, confirmButton = { TextButton(onClick = { onRecord(amount.toLongOrNull() ?: 0L, description) }, enabled = amount.toLongOrNull()?.let { it > 0 } == true && description.isNotBlank()) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun ExchangeDialog(
    accounts: List<FinancialAccount>,
    onDismiss: () -> Unit,
    onExchange: (FinancialAccount, FinancialAccount, Long, Long, String) -> Unit,
) {
    val source = accounts.firstOrNull()
    val destination = accounts.firstOrNull { it.currency != source?.currency }
    var sourceAmount by remember { mutableStateOf("") }
    var destinationAmount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val validRate = runCatching { BigDecimal(rate).signum() > 0 }.getOrDefault(false)
    val sourceMinor = sourceAmount.toLongOrNull()
    val destinationMinor = destinationAmount.toLongOrNull()
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.exchange_transaction)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            Text("${stringResource(R.string.select_source)}: ${source?.name.orEmpty()}")
            Text("${stringResource(R.string.select_destination)}: ${destination?.name.orEmpty()}")
            OutlinedTextField(sourceAmount, { sourceAmount = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.amount)) }, singleLine = true)
            OutlinedTextField(destinationAmount, { destinationAmount = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.target_amount)) }, singleLine = true)
            OutlinedTextField(rate, { rate = it }, label = { Text(stringResource(R.string.exchange_rate)) }, singleLine = true)
            OutlinedTextField(fee, { fee = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.fee)) }, singleLine = true)
            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.notes)) }, singleLine = true)
            if (source != null && destination != null && sourceMinor != null && destinationMinor != null) Text(stringResource(R.string.exchange_preview, source.name, "$sourceMinor ${source.currency.isoCode}", destination.name, "$destinationMinor ${destination.currency.isoCode}"), style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = { if (source != null && destination != null) onExchange(source, destination, sourceMinor ?: 0L, destinationMinor ?: 0L, notes.ifBlank { "Currency exchange at $rate; fee ${fee.ifBlank { "0" }}" }) }, enabled = source != null && destination != null && sourceMinor?.let { it > 0 } == true && destinationMinor?.let { it > 0 } == true && validRate) { Text(stringResource(R.string.confirm)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
