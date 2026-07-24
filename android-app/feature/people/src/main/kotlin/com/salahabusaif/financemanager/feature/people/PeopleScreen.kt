package com.salahabusaif.financemanager.feature.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.MoneyText
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.ledger.CreatePersonCommand
import com.salahabusaif.financemanager.core.ledger.FinancialAccount
import com.salahabusaif.financemanager.core.ledger.InsufficientFundsSettlement
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.ledger.PeopleGateway
import com.salahabusaif.financemanager.core.ledger.Person
import com.salahabusaif.financemanager.core.ledger.PersonMoneyCommand
import com.salahabusaif.financemanager.core.ledger.PersonOperation
import com.salahabusaif.financemanager.core.ledger.PersonOperationType
import com.salahabusaif.financemanager.core.ledger.PersonSummary
import com.salahabusaif.financemanager.core.ledger.PersonStatement
import com.salahabusaif.financemanager.core.ledger.PersonTransferCommand
import com.salahabusaif.financemanager.core.model.CurrencyCode
import com.salahabusaif.financemanager.core.money.Money
import com.salahabusaif.financemanager.core.money.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import android.content.Intent
import android.util.Log
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val peopleGateway: PeopleGateway,
    private val ledgerGateway: LedgerGateway,
) : ViewModel() {
    val people = peopleGateway.people.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val accounts = ledgerGateway.financialAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, nickname: String, phone: String, aliases: String, notes: String) = viewModelScope.launch {
        peopleGateway.createPerson(
            CreatePersonCommand(name, nickname, phone, aliases.split(',').map(String::trim), notes = notes),
        )
    }

    fun operations(personId: String, currency: CurrencyCode) =
        peopleGateway.personOperations(personId, currency).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun post(type: PersonOperationType, personId: String, accountId: String, amountMinor: Long, notes: String, beneficiary: String = "", commissionMinor: Long = 0, settlement: InsufficientFundsSettlement = InsufficientFundsSettlement.REJECT) = viewModelScope.launch {
        val result = when (type) {
            PersonOperationType.DEPOSIT -> peopleGateway.deposit(PersonMoneyCommand(UUID.randomUUID().toString(), personId, accountId, amountMinor, notes))
            PersonOperationType.WITHDRAWAL -> peopleGateway.withdraw(PersonMoneyCommand(UUID.randomUUID().toString(), personId, accountId, amountMinor, notes))
            PersonOperationType.LOAN -> peopleGateway.loan(PersonMoneyCommand(UUID.randomUUID().toString(), personId, accountId, amountMinor, notes))
            PersonOperationType.REPAYMENT -> peopleGateway.repay(PersonMoneyCommand(UUID.randomUUID().toString(), personId, accountId, amountMinor, notes))
            PersonOperationType.TRANSFER -> peopleGateway.transfer(PersonTransferCommand(UUID.randomUUID().toString(), personId, accountId, amountMinor, commissionMinor, beneficiary, notes, settlement))
        }
        result.exceptionOrNull()?.let { error -> Log.e("PeopleOperations", "Failed to post $type", error) }
    }

    fun statement(personId: String, currency: CurrencyCode, onReady: (PersonStatement) -> Unit) = viewModelScope.launch {
        peopleGateway.statement(personId, currency, 0, Long.MAX_VALUE).onSuccess(onReady)
    }
}

@Composable
fun PeopleScreen(viewModel: PeopleViewModel = hiltViewModel()) {
    val people by viewModel.people.collectAsState()
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<PersonSummary?>(null) }
    when {
        adding -> PersonEditor(onDismiss = { adding = false }) { name, nickname, phone, aliases, notes ->
            viewModel.create(name, nickname, phone, aliases, notes)
            adding = false
        }
        selected != null -> PersonProfile(requireNotNull(selected), viewModel, onBack = { selected = null })
        else -> PeopleList(people, query, { query = it }, onAdd = { adding = true }, onOpen = { selected = it })
    }
}

@Composable
private fun PeopleList(
    people: List<PersonSummary>,
    query: String,
    onQuery: (String) -> Unit,
    onAdd: () -> Unit,
    onOpen: (PersonSummary) -> Unit,
) {
    val filtered = people.filter { summary ->
        val needle = query.trim().lowercase()
        needle.isBlank() || summary.person.displayName.lowercase().contains(needle) || summary.person.aliases.any { it.lowercase().contains(needle) }
    }
    Column(Modifier.fillMaxSize().padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md)) {
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth().testTag("people_search"), label = { Text(stringResource(R.string.people_search_hint)) }, singleLine = true)
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().testTag("add_person")) { Icon(Icons.Default.Add, null); Text(stringResource(R.string.add_person_action)) }
        if (filtered.isEmpty()) {
            EmptyState(stringResource(R.string.no_people), stringResource(R.string.no_people_detail), Icons.Default.People)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
                items(filtered, key = { it.person.id }) { summary -> PersonCard(summary) { onOpen(summary) } }
            }
        }
    }
}

@Composable
private fun PersonCard(summary: PersonSummary, onOpen: () -> Unit) = Card(onClick = onOpen, modifier = Modifier.fillMaxWidth().testTag("person_${summary.person.id}")) {
    Column(Modifier.padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.xs)) {
        Text(summary.person.displayName)
        summary.person.nickname?.let { Text(it) }
        summary.balances.filter { it.fundsHeldMinor != 0L || it.receivableMinor != 0L }.forEach { balance ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(stringResource(R.string.money_held)); MoneyText(Money(balance.fundsHeldMinor, balance.currency)) }
                Column { Text(stringResource(R.string.owes_me)); MoneyText(Money(balance.receivableMinor, balance.currency)) }
            }
        }
    }
}

@Composable
private fun PersonEditor(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aliases by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
        Text(stringResource(R.string.add_person_action))
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth().testTag("person_name"), label = { Text(stringResource(R.string.full_name)) }, isError = name.isBlank())
        OutlinedTextField(nickname, { nickname = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.nickname)) })
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.phone_optional)) })
        OutlinedTextField(aliases, { aliases = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.aliases_optional)) })
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.person_notes_optional)) })
        Row(horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            Button(onClick = { onSave(name, nickname, phone, aliases, notes) }, enabled = name.isNotBlank(), modifier = Modifier.testTag("save_person")) { Text(stringResource(R.string.save_person)) }
        }
    }
}

@Composable
private fun PersonProfile(summary: PersonSummary, viewModel: PeopleViewModel, onBack: () -> Unit) {
    var currency by remember { mutableStateOf(CurrencyCode.ILS) }
    var action by remember { mutableStateOf<PersonOperationType?>(null) }
    var statement by remember { mutableStateOf<PersonStatement?>(null) }
    val operations by viewModel.operations(summary.person.id, currency).collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    Column(Modifier.fillMaxSize().padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
        Text(summary.person.displayName)
        Row(horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            CurrencyCode.entries.forEach { item ->
                TextButton(onClick = { currency = item }) { Text(stringResource(item.labelRes())) }
            }
        }
        val balance = summary.balances.first { it.currency == currency }
        Text(stringResource(R.string.money_held)); MoneyText(Money(balance.fundsHeldMinor, currency))
        Text(stringResource(R.string.owes_me)); MoneyText(Money(balance.receivableMinor, currency))
        Row(horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.xs)) {
            ActionButton(R.string.add_deposit) { action = PersonOperationType.DEPOSIT }
            ActionButton(R.string.record_withdrawal) { action = PersonOperationType.WITHDRAWAL }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.xs)) {
            ActionButton(R.string.record_loan) { action = PersonOperationType.LOAN }
            ActionButton(R.string.record_repayment) { action = PersonOperationType.REPAYMENT }
            ActionButton(R.string.transfer_for_person) { action = PersonOperationType.TRANSFER }
        }
        ActionButton(R.string.generate_statement) { viewModel.statement(summary.person.id, currency) { statement = it } }
        Text(stringResource(R.string.recent_activity))
        if (operations.isEmpty()) Text(stringResource(R.string.no_person_activity))
        else LazyColumn { items(operations, key = PersonOperation::id) { operation -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(operation.type.labelRes())); MoneyText(Money(operation.amountMinor, operation.currency)) } } }
    }
    action?.let { type -> PersonOperationDialog(type, summary.person, currency, accounts, { action = null }) { account, amount, notes, beneficiary, commission, settlement ->
        viewModel.post(type, summary.person.id, account.id, amount, notes, beneficiary, commission, settlement)
        action = null
    } }
    statement?.let { PersonStatementDialog(it) { statement = null } }
}

@Composable
private fun ActionButton(label: Int, onClick: () -> Unit) = TextButton(onClick = onClick) { Text(stringResource(label)) }

@Composable
private fun PersonOperationDialog(
    type: PersonOperationType,
    person: Person,
    currency: CurrencyCode,
    accounts: List<FinancialAccount>,
    onDismiss: () -> Unit,
    onConfirm: (FinancialAccount, Long, String, String, Long, InsufficientFundsSettlement) -> Unit,
) {
    val eligible = accounts.filter { !it.isArchived && it.currency == currency }
    var selected by remember(type, currency) { mutableStateOf(eligible.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var settlement by remember { mutableStateOf(InsufficientFundsSettlement.REJECT) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(type.labelRes())) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
                Text(person.displayName)
                if (eligible.isEmpty()) {
                    Text(stringResource(R.string.no_eligible_accounts))
                } else {
                    eligible.forEach { account ->
                        TextButton(onClick = { selected = account }) {
                            Text(if (selected?.id == account.id) "✓ ${account.name}" else account.name)
                        }
                    }
                }
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.amount)) }, singleLine = true)
                if (type == PersonOperationType.TRANSFER) {
                    OutlinedTextField(beneficiary, { beneficiary = it }, label = { Text(stringResource(R.string.beneficiary)) }, singleLine = true)
                    OutlinedTextField(commission, { commission = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.fee)) }, singleLine = true)
                    Text(stringResource(R.string.insufficient_funds_choice))
                    SettlementChoice(settlement == InsufficientFundsSettlement.REJECT, R.string.cancel_if_insufficient) { settlement = InsufficientFundsSettlement.REJECT }
                    SettlementChoice(settlement == InsufficientFundsSettlement.AVAILABLE_FUNDS_AND_RECEIVABLE, R.string.use_held_and_receivable) { settlement = InsufficientFundsSettlement.AVAILABLE_FUNDS_AND_RECEIVABLE }
                    SettlementChoice(settlement == InsufficientFundsSettlement.FULL_RECEIVABLE, R.string.create_full_receivable) { settlement = InsufficientFundsSettlement.FULL_RECEIVABLE }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.notes)) })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let { onConfirm(it, amount.toLongOrNull() ?: 0, notes, beneficiary, commission.toLongOrNull() ?: 0, settlement) } },
                enabled = selected != null && amount.toLongOrNull()?.let { it > 0 } == true && (type != PersonOperationType.TRANSFER || beneficiary.isNotBlank()),
            ) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun SettlementChoice(selected: Boolean, label: Int, onClick: () -> Unit) = TextButton(onClick = onClick) {
    Text(if (selected) "✓ ${stringResource(label)}" else stringResource(label))
}

@Composable
private fun PersonStatementDialog(statement: PersonStatement, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val openingHeld = stringResource(R.string.statement_opening_held)
    val openingReceivable = stringResource(R.string.statement_opening_receivable)
    val closingHeld = stringResource(R.string.statement_closing_held)
    val closingReceivable = stringResource(R.string.statement_closing_receivable)
    val beneficiaryLabel = stringResource(R.string.beneficiary)
    val commissionLabel = stringResource(R.string.fee)
    val statementDate = stringResource(R.string.statement_date)
    val operationLabels = mapOf(
        PersonOperationType.DEPOSIT to stringResource(R.string.person_deposit),
        PersonOperationType.WITHDRAWAL to stringResource(R.string.person_withdrawal),
        PersonOperationType.LOAN to stringResource(R.string.person_loan),
        PersonOperationType.REPAYMENT to stringResource(R.string.person_repayment),
        PersonOperationType.TRANSFER to stringResource(R.string.person_transfer),
    )
    val locale = LocalConfiguration.current.locales[0]
    fun format(amount: Long) = MoneyFormatter.format(Money(amount, statement.currency), locale)
    val operationLines = statement.operations.joinToString("\n") { operation ->
        buildString {
            append(operationLabels.getValue(operation.type)).append(": ").append(format(operation.amountMinor))
            operation.beneficiaryName?.takeIf(String::isNotBlank)?.let { append(" — ").append(beneficiaryLabel).append(": ").append(it) }
            if (operation.commissionMinor > 0) append(" — ").append(commissionLabel).append(": ").append(format(operation.commissionMinor))
            append(" — ").append(statementDate).append(": ").append(DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(operation.occurredAt)))
        }
    }
    val plainText = buildString {
        append(statement.person.displayName).append('\n')
        append(openingHeld).append(": ").append(format(statement.openingFundsHeldMinor)).append('\n')
        append(openingReceivable).append(": ").append(format(statement.openingReceivableMinor)).append('\n')
        if (operationLines.isNotBlank()) append(operationLines).append('\n')
        append(closingHeld).append(": ").append(format(statement.closingFundsHeldMinor)).append('\n')
        append(closingReceivable).append(": ").append(format(statement.closingReceivableMinor))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.generate_statement)) },
        text = { Text(plainText) },
        confirmButton = { TextButton(onClick = { clipboard.setText(AnnotatedString(plainText)) }) { Text(stringResource(R.string.copy)) } },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, plainText), context.getString(R.string.share)))
                }) { Text(stringResource(R.string.share)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

private fun CurrencyCode.labelRes() = when (this) {
    CurrencyCode.ILS -> R.string.currency_ils
    CurrencyCode.USD -> R.string.currency_usd
    CurrencyCode.JOD -> R.string.currency_jod
}

private fun com.salahabusaif.financemanager.core.ledger.PersonOperationType.labelRes() = when (this) {
    com.salahabusaif.financemanager.core.ledger.PersonOperationType.DEPOSIT -> R.string.person_deposit
    com.salahabusaif.financemanager.core.ledger.PersonOperationType.WITHDRAWAL -> R.string.person_withdrawal
    com.salahabusaif.financemanager.core.ledger.PersonOperationType.LOAN -> R.string.person_loan
    com.salahabusaif.financemanager.core.ledger.PersonOperationType.REPAYMENT -> R.string.person_repayment
    com.salahabusaif.financemanager.core.ledger.PersonOperationType.TRANSFER -> R.string.person_transfer
}
