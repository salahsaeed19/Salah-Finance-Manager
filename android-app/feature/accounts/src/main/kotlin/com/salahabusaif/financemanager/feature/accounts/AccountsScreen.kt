package com.salahabusaif.financemanager.feature.accounts

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.salahabusaif.financemanager.core.data.profile.OwnerProfile
import com.salahabusaif.financemanager.core.data.profile.OwnerProfileRepository
import com.salahabusaif.financemanager.core.designsystem.FinanceRadius
import com.salahabusaif.financemanager.core.designsystem.FinanceSectionHeader
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.MoneyText
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.ledger.AccountProvider
import com.salahabusaif.financemanager.core.ledger.CreateFinancialAccountCommand
import com.salahabusaif.financemanager.core.ledger.FinancialAccount
import com.salahabusaif.financemanager.core.ledger.LedgerAccountRole
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.ledger.UpdateFinancialAccountCommand
import com.salahabusaif.financemanager.core.model.CurrencyCode
import com.salahabusaif.financemanager.core.money.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val ledgerGateway: LedgerGateway,
    private val ownerProfileRepository: OwnerProfileRepository,
) : ViewModel() {
    val accounts = ledgerGateway.financialAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val ownerProfile = ownerProfileRepository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OwnerProfile())

    init {
        viewModelScope.launch { ownerProfileRepository.bootstrapIfAbsent() }
    }

    fun save(slot: AccountSlot, existing: FinancialAccount?, name: String, identifier: String, openingBalance: Long) = viewModelScope.launch {
        if (existing == null) {
            ledgerGateway.createFinancialAccount(
                CreateFinancialAccountCommand(
                    operationId = UUID.randomUUID().toString(),
                    name = name,
                    kind = slot.role,
                    currency = slot.currency,
                    institutionName = null,
                    provider = slot.provider,
                    maskedAccountNumber = identifier.ifBlank { null },
                    openingBalanceMinor = openingBalance,
                ),
            )
        } else {
            ledgerGateway.updateFinancialAccount(
                UpdateFinancialAccountCommand(
                    id = existing.id,
                    name = name,
                    institutionName = null,
                    maskedAccountNumber = identifier.ifBlank { null },
                ),
            )
        }
    }
}

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsState()
    val ownerProfile by viewModel.ownerProfile.collectAsState()
    var setup by remember { mutableStateOf<AccountSlot?>(null) }
    if (setup != null) {
        val slot = requireNotNull(setup)
        val existing = accounts.firstOrNull { !it.isArchived && it.provider == slot.provider && it.currency == slot.currency }
        AccountConfigurationScreen(
            slot = slot,
            existing = existing,
            ownerProfile = ownerProfile,
            onBack = { setup = null },
            onSave = { name, identifier, opening ->
                viewModel.save(slot, existing, name, identifier, opening)
                setup = null
            },
        )
    } else {
        PersonalAccountsOverview(
            accounts = accounts,
            ownerProfile = ownerProfile,
            onConfigure = { setup = it },
            onBack = onBack,
        )
    }
}

@Composable
private fun PersonalAccountsOverview(
    accounts: List<FinancialAccount>,
    ownerProfile: OwnerProfile,
    onConfigure: (AccountSlot) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FinanceSpacing.md).testTag("create_account"),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
            }
            Column {
                FinanceSectionHeader(stringResource(R.string.my_accounts))
                Text(stringResource(R.string.account_setup_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (ownerProfile.fullName.isNotBlank()) {
            Text(
                text = ownerProfile.fullName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        accountGroups().forEach { group ->
            ProviderGroupCard(
                group = group,
                accounts = accounts,
                onConfigure = onConfigure,
            )
        }
    }
}

@Composable
private fun ProviderGroupCard(
    group: AccountGroup,
    accounts: List<FinancialAccount>,
    onConfigure: (AccountSlot) -> Unit,
) = Card(
    modifier = Modifier.fillMaxWidth(),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(FinanceRadius.large),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
) {
    Column(Modifier.padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            Icon(
                group.icon,
                contentDescription = stringResource(group.label),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(stringResource(group.label), style = MaterialTheme.typography.titleMedium)
        }
        group.slots.forEachIndexed { index, slot ->
            if (index > 0) HorizontalDivider()
            val account = accounts.firstOrNull { !it.isArchived && it.provider == slot.provider && it.currency == slot.currency }
            CurrencyAccountRow(slot, account, onConfigure)
        }
    }
}

@Composable
private fun CurrencyAccountRow(
    slot: AccountSlot,
    account: FinancialAccount?,
    onConfigure: (AccountSlot) -> Unit,
) = Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = FinanceSpacing.xs),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm),
) {
    val configureDescription = stringResource(R.string.configure)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(currencyLabel(slot.currency), style = MaterialTheme.typography.titleSmall)
        if (account == null) {
            Text(stringResource(R.string.not_configured), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            account.maskedAccountNumber?.takeIf(String::isNotBlank)?.let {
                Text(maskIdentifier(it), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            MoneyText(Money(account.balanceMinor, account.currency))
        }
    }
    if (account == null) {
        Button(
            onClick = { onConfigure(slot) },
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = configureDescription },
        ) { Text(stringResource(R.string.configure)) }
    } else {
        TextButton(
            onClick = { onConfigure(slot) },
            modifier = Modifier.heightIn(min = 48.dp),
        ) { Text(stringResource(R.string.edit_account)) }
    }
}

@Composable
private fun AccountConfigurationScreen(
    slot: AccountSlot,
    existing: FinancialAccount?,
    ownerProfile: OwnerProfile,
    onBack: () -> Unit,
    onSave: (String, String, Long) -> Unit,
) {
    val defaultName = "${stringResource(slot.providerLabel)} — ${currencyLabel(slot.currency)}"
    val defaultIdentifier = when (slot.provider) {
        AccountProvider.BANK_OF_PALESTINE -> ownerProfile.bankOfPalestineReference
        AccountProvider.JAWWAL_PAY, AccountProvider.PALPAY -> ownerProfile.phoneNumber
        AccountProvider.CASH -> ""
    }
    var name by remember(slot, existing) { mutableStateOf(existing?.name ?: defaultName) }
    var identifier by remember(slot, existing, ownerProfile) { mutableStateOf(existing?.maskedAccountNumber ?: defaultIdentifier) }
    var opening by remember(slot, existing) { mutableStateOf("") }
    val openingMinor = opening.toLongOrNull() ?: 0L
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth().padding(FinanceSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
            }
            Column {
                Text(stringResource(R.string.configure_account), style = MaterialTheme.typography.titleLarge)
                Text(
                    "${stringResource(slot.providerLabel)} · ${currencyLabel(slot.currency)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(
            Modifier.padding(horizontal = FinanceSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
        ) {
            FinanceSectionHeader(stringResource(R.string.account_information))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.account_name)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = ownerProfile.fullName,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.account_holder)) },
                enabled = false,
                singleLine = true,
            )
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(identifierLabel(slot.provider)) },
                singleLine = true,
            )
            if (existing == null) {
                OutlinedTextField(
                    value = opening,
                    onValueChange = { opening = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.opening_balance_optional)) },
                    singleLine = true,
                )
                Text(stringResource(R.string.opening_balance_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (openingMinor > 0L) {
                    Text(
                        stringResource(R.string.opening_preview, "$openingMinor ${currencyLabel(slot.currency)}"),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(FinanceSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm),
        ) {
            Button(
                onClick = { onSave(name, identifier, openingMinor) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(if (existing == null) R.string.configure else R.string.save)) }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.cancel)) }
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

@Composable
private fun identifierLabel(provider: AccountProvider): String = stringResource(
    if (provider == AccountProvider.BANK_OF_PALESTINE) R.string.masked_account else R.string.phone_identifier,
)

private fun maskIdentifier(value: String): String =
    if (value.length <= 4) "••••" else "•••• ${value.takeLast(4)}"

data class AccountSlot(
    val provider: AccountProvider,
    val role: LedgerAccountRole,
    val currency: CurrencyCode,
    @StringRes val providerLabel: Int,
)

private data class AccountGroup(
    @StringRes val label: Int,
    val icon: ImageVector,
    val slots: List<AccountSlot>,
)

private fun accountGroups(): List<AccountGroup> = listOf(
    AccountGroup(
        label = R.string.bank_of_palestine,
        icon = Icons.Default.AccountBalance,
        slots = CurrencyCode.entries.map { AccountSlot(AccountProvider.BANK_OF_PALESTINE, LedgerAccountRole.BANK, it, R.string.bank_of_palestine) },
    ),
    AccountGroup(
        label = R.string.jawwal_pay,
        icon = Icons.Default.PhoneAndroid,
        slots = listOf(AccountSlot(AccountProvider.JAWWAL_PAY, LedgerAccountRole.WALLET, CurrencyCode.ILS, R.string.jawwal_pay)),
    ),
    AccountGroup(
        label = R.string.palpay,
        icon = Icons.Default.Payments,
        slots = listOf(AccountSlot(AccountProvider.PALPAY, LedgerAccountRole.WALLET, CurrencyCode.ILS, R.string.palpay)),
    ),
    AccountGroup(
        label = R.string.cash_group,
        icon = Icons.Default.AccountBalanceWallet,
        slots = CurrencyCode.entries.map { AccountSlot(AccountProvider.CASH, LedgerAccountRole.CASH, it, R.string.cash_group) },
    ),
)
