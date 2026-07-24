package com.salahabusaif.financemanager.core.data.ledger

import androidx.room.withTransaction
import com.salahabusaif.financemanager.core.database.FinanceDatabase
import com.salahabusaif.financemanager.core.database.dao.FinancialAccountRow
import com.salahabusaif.financemanager.core.database.dao.LedgerTransactionRow
import com.salahabusaif.financemanager.core.database.entity.AuditEventEntity
import com.salahabusaif.financemanager.core.database.entity.FinancialAccountEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerPostingEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerTransactionEntity
import com.salahabusaif.financemanager.core.database.entity.TransactionGroupEntity
import com.salahabusaif.financemanager.core.ledger.CreateFinancialAccountCommand
import com.salahabusaif.financemanager.core.ledger.AccountProvider
import com.salahabusaif.financemanager.core.ledger.CurrencyExchangePlan
import com.salahabusaif.financemanager.core.ledger.FinancialAccount
import com.salahabusaif.financemanager.core.ledger.LedgerAccount
import com.salahabusaif.financemanager.core.ledger.LedgerAccountRole
import com.salahabusaif.financemanager.core.ledger.LedgerAccountType
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.ledger.LedgerTransactionStatus
import com.salahabusaif.financemanager.core.ledger.LedgerTransactionSummary
import com.salahabusaif.financemanager.core.ledger.LedgerTransactionType
import com.salahabusaif.financemanager.core.ledger.LedgerValidationResult
import com.salahabusaif.financemanager.core.ledger.NormalBalance
import com.salahabusaif.financemanager.core.ledger.PostingEngine
import com.salahabusaif.financemanager.core.ledger.PostingPlan
import com.salahabusaif.financemanager.core.ledger.RecordTransactionCommand
import com.salahabusaif.financemanager.core.ledger.UpdateFinancialAccountCommand
import com.salahabusaif.financemanager.core.ledger.TransferCommand
import com.salahabusaif.financemanager.core.ledger.ExchangeCommand
import com.salahabusaif.financemanager.core.model.CurrencyCode
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomLedgerGateway @Inject constructor(
    private val database: FinanceDatabase,
) : LedgerGateway {
    override val financialAccounts: Flow<List<FinancialAccount>> =
        database.ledgerDao().financialAccounts().map { rows -> rows.map(FinancialAccountRow::toDomain) }

    override val transactions: Flow<List<LedgerTransactionSummary>> =
        database.ledgerDao().transactionSummaries().map { rows -> rows.map(LedgerTransactionRow::toDomain) }

    override suspend fun createFinancialAccount(command: CreateFinancialAccountCommand): Result<FinancialAccount> =
        runCatching {
            require(command.name.isNotBlank()) { "Account name is required." }
            require(command.kind in realAccountRoles) { "Unsupported financial account role." }
            require(command.openingBalanceMinor >= 0) { "Opening balance cannot be negative." }
            val now = System.currentTimeMillis()
            val ledgerAccount = LedgerAccount(
                id = UUID.randomUUID().toString(),
                currency = command.currency,
                type = LedgerAccountType.ASSET,
                role = command.kind,
            )
            val financialId = UUID.randomUUID().toString()
            database.withTransaction {
                val dao = database.ledgerDao()
                dao.seedAccounts(SystemLedgerAccounts.entities(now))
                dao.insertAccounts(listOf(ledgerAccount.toEntity(command.name, now)))
                dao.insertFinancialAccount(
                    FinancialAccountEntity(
                        id = financialId,
                        ledgerAccountId = ledgerAccount.id,
                        institutionName = command.institutionName,
                        accountKind = command.kind.name,
                        maskedAccountNumber = command.maskedAccountNumber,
                        providerCode = command.provider?.name,
                        colorToken = null,
                        iconToken = null,
                        isArchived = false,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
                if (command.openingBalanceMinor > 0) {
                    writePlan(openingPlan(command, ledgerAccount), now)
                }
                audit("financial_account", financialId, "CREATED", now)
            }
            FinancialAccount(
                id = financialId,
                name = command.name,
                kind = command.kind,
                currency = command.currency,
                institutionName = command.institutionName,
                provider = command.provider,
                maskedAccountNumber = command.maskedAccountNumber,
                balanceMinor = command.openingBalanceMinor,
            )
        }

    override suspend fun updateFinancialAccount(command: UpdateFinancialAccountCommand): Result<Unit> = runCatching {
        require(command.name.isNotBlank()) { "Account name is required." }
        val now = System.currentTimeMillis()
        database.withTransaction {
            val dao = database.ledgerDao()
            require(dao.renameFinancialLedgerAccount(command.id, command.name, now) == 1) { "Account not found." }
            dao.updateFinancialAccountDetails(command.id, command.institutionName, command.maskedAccountNumber, now)
            audit("financial_account", command.id, "UPDATED", now)
        }
    }

    override suspend fun archiveFinancialAccount(id: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        database.withTransaction {
            require(database.ledgerDao().setFinancialAccountArchived(id, true, now) == 1) { "Account not found." }
            audit("financial_account", id, "ARCHIVED", now)
        }
    }

    override suspend fun post(plan: PostingPlan): Result<Unit> = runCatching {
        require(PostingEngine.validate(plan) is LedgerValidationResult.Valid) { "Invalid ledger posting plan." }
        database.withTransaction {
            database.ledgerDao().seedAccounts(SystemLedgerAccounts.entities(System.currentTimeMillis()))
            writePlan(plan, System.currentTimeMillis())
        }
    }

    override suspend fun recordPersonalTransaction(command: RecordTransactionCommand): Result<Unit> = runCatching {
        require(command.amountMinor > 0) { "Amount must be positive." }
        require(command.description.isNotBlank()) { "Description is required." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val now = System.currentTimeMillis()
            dao.seedAccounts(SystemLedgerAccounts.entities(now))
            val account = requireNotNull(dao.ledgerAccountForFinancial(command.financialAccountId)) { "Account not found." }.toDomain()
            val plan = when (command.type) {
                LedgerTransactionType.SALARY_INCOME -> PostingEngine.personalIncome(command.operationId, account, SystemLedgerAccounts.salaryIncome(account.currency), command.amountMinor, true)
                LedgerTransactionType.OTHER_INCOME -> PostingEngine.personalIncome(command.operationId, account, SystemLedgerAccounts.otherIncome(account.currency), command.amountMinor, false)
                LedgerTransactionType.PERSONAL_EXPENSE -> PostingEngine.personalExpense(command.operationId, SystemLedgerAccounts.expense(account.currency), account, command.amountMinor)
                else -> error("Unsupported personal transaction type.")
            }.copy(description = command.description)
            writePlan(plan, now)
        }
    }

    override suspend fun recordTransfer(command: TransferCommand): Result<Unit> = runCatching {
        require(command.amountMinor > 0) { "Amount must be positive." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val now = System.currentTimeMillis()
            val source = requireNotNull(dao.ledgerAccountForFinancial(command.sourceFinancialAccountId)) { "Source account not found." }.toDomain()
            val destination = requireNotNull(dao.ledgerAccountForFinancial(command.destinationFinancialAccountId)) { "Destination account not found." }.toDomain()
            require(dao.postedBalance(source.id) >= command.amountMinor) { "Source account has insufficient funds." }
            writePlan(PostingEngine.internalTransfer(command.operationId, source, destination, command.amountMinor).copy(description = command.description), now)
        }
    }

    override suspend fun recordExchange(command: ExchangeCommand): Result<Unit> = runCatching {
        require(command.sourceAmountMinor > 0 && command.destinationAmountMinor > 0) { "Amounts must be positive." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val source = requireNotNull(dao.ledgerAccountForFinancial(command.sourceFinancialAccountId)) { "Source account not found." }.toDomain()
            val destination = requireNotNull(dao.ledgerAccountForFinancial(command.destinationFinancialAccountId)) { "Destination account not found." }.toDomain()
            require(dao.postedBalance(source.id) >= command.sourceAmountMinor) { "Source account has insufficient funds." }
            val plan = PostingEngine.currencyExchange(command.operationId, source, SystemLedgerAccounts.fxClearing(source.currency), SystemLedgerAccounts.fxClearing(destination.currency), destination, command.sourceAmountMinor, command.destinationAmountMinor, command.description)
            postExchange(plan).getOrThrow()
        }
    }

    override suspend fun postExchange(plan: CurrencyExchangePlan): Result<Unit> = runCatching {
        require(PostingEngine.validate(plan.sourcePlan) is LedgerValidationResult.Valid)
        require(PostingEngine.validate(plan.destinationPlan) is LedgerValidationResult.Valid)
        database.withTransaction {
            val dao = database.ledgerDao()
            val now = System.currentTimeMillis()
            dao.seedAccounts(SystemLedgerAccounts.entities(now))
            val group = TransactionGroupEntity(UUID.randomUUID().toString(), "CURRENCY_EXCHANGE", plan.groupOperationId, plan.description, now)
            dao.insertTransactionGroup(group)
            writePlan(plan.sourcePlan, now, group.id)
            writePlan(plan.destinationPlan, now, group.id)
            audit("transaction_group", group.id, "POSTED", now)
        }
    }

    override suspend fun reverse(transactionId: String, operationId: String): Result<Unit> = runCatching {
        database.withTransaction {
            val dao = database.ledgerDao()
            val original = requireNotNull(dao.transaction(transactionId)) { "Transaction not found." }
            require(original.status == LedgerTransactionStatus.POSTED.name) { "Only posted transactions can be reversed." }
            val originalPostings = dao.postingsForTransaction(transactionId)
            require(originalPostings.isNotEmpty()) { "Transaction has no postings." }
            val currency = CurrencyCode.valueOf(originalPostings.first().currencyCode)
            val plan = PostingPlan(
                operationId = operationId,
                type = LedgerTransactionType.REVERSAL,
                currency = currency,
                description = "Reversal: ${original.description}",
                postings = originalPostings.map { posting ->
                    com.salahabusaif.financemanager.core.ledger.Posting(
                        account = requireNotNull(dao.account(posting.ledgerAccountId)).toDomain(),
                        direction = if (posting.direction == "DEBIT") com.salahabusaif.financemanager.core.ledger.PostingDirection.CREDIT else com.salahabusaif.financemanager.core.ledger.PostingDirection.DEBIT,
                        amountMinor = posting.amountMinor,
                        memo = posting.memo,
                    )
                },
                reversalOfId = original.id,
            )
            writePlan(plan, System.currentTimeMillis())
            audit("ledger_transaction", original.id, "REVERSED", System.currentTimeMillis())
        }
    }

    private suspend fun writePlan(plan: PostingPlan, now: Long, groupId: String? = null) {
        val dao = database.ledgerDao()
        require(dao.transactionByOperationId(plan.operationId) == null) { "Duplicate operation ID." }
        val transactionId = UUID.randomUUID().toString()
        dao.postAtomically(
            LedgerTransactionEntity(
                id = transactionId,
                groupId = groupId,
                operationId = plan.operationId,
                transactionType = plan.type.name,
                status = LedgerTransactionStatus.POSTED.name,
                accountingDate = now / millisPerDay,
                occurredAt = now,
                description = plan.description,
                notes = null,
                reversalOfId = plan.reversalOfId,
                replacedById = plan.replacementOfId,
                postedAt = now,
                createdAt = now,
                updatedAt = now,
                version = 1,
            ),
            plan.postings.mapIndexed { index, posting ->
                LedgerPostingEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    ledgerAccountId = posting.account.id,
                    direction = posting.direction.name,
                    amountMinor = posting.amountMinor,
                    currencyCode = posting.account.currency.isoCode,
                    memo = posting.memo,
                    sequence = index,
                    createdAt = now,
                )
            },
        )
        audit("ledger_transaction", transactionId, "POSTED", now)
    }

    private suspend fun audit(entityType: String, entityId: String, eventType: String, now: Long) {
        database.ledgerDao().insertAuditEvent(
            AuditEventEntity(UUID.randomUUID().toString(), entityType, entityId, eventType, null, now),
        )
    }

    private fun openingPlan(command: CreateFinancialAccountCommand, account: LedgerAccount): PostingPlan =
        PostingEngine.openingBalance(
            operationId = "${command.operationId}-opening",
            asset = account,
            equity = SystemLedgerAccounts.openingEquity(command.currency),
            amountMinor = command.openingBalanceMinor,
        )
}

private object SystemLedgerAccounts {
    fun openingEquity(currency: CurrencyCode) = systemAccount("opening-equity", currency, LedgerAccountType.EQUITY, LedgerAccountRole.OPENING_BALANCE_EQUITY)

    fun salaryIncome(currency: CurrencyCode) = systemAccount("salary-income", currency, LedgerAccountType.INCOME, LedgerAccountRole.SALARY_INCOME)

    fun otherIncome(currency: CurrencyCode) = systemAccount("other-income", currency, LedgerAccountType.INCOME, LedgerAccountRole.OTHER_INCOME)

    fun expense(currency: CurrencyCode) = systemAccount("expense", currency, LedgerAccountType.EXPENSE, LedgerAccountRole.EXPENSE_CATEGORY)

    fun fxClearing(currency: CurrencyCode) = systemAccount("fx-clearing", currency, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING)

    fun entities(now: Long): List<LedgerAccountEntity> = CurrencyCode.entries.flatMap { currency ->
        listOf(
            openingEquity(currency),
            salaryIncome(currency),
            otherIncome(currency),
            systemAccount("commission-income", currency, LedgerAccountType.INCOME, LedgerAccountRole.COMMISSION_INCOME),
            expense(currency),
            fxClearing(currency),
            systemAccount("transfer-clearing", currency, LedgerAccountType.CLEARING, LedgerAccountRole.TRANSFER_CLEARING),
        ).map { account -> account.toEntity("System ${account.role.name}", now, true) }
    }

    private fun systemAccount(prefix: String, currency: CurrencyCode, type: LedgerAccountType, role: LedgerAccountRole) =
        LedgerAccount("$prefix-${currency.isoCode}", currency, type, role)
}

private fun LedgerAccount.toEntity(name: String, now: Long, isSystem: Boolean = false) = LedgerAccountEntity(
    id = id,
    code = if (isSystem) id else "manual-$id",
    name = name,
    type = type.name,
    role = role.name,
    currencyCode = currency.isoCode,
    normalBalance = if (type == LedgerAccountType.ASSET || type == LedgerAccountType.EXPENSE) NormalBalance.DEBIT.name else NormalBalance.CREDIT.name,
    isSystem = isSystem,
    isActive = true,
    createdAt = now,
    updatedAt = now,
)

private fun LedgerAccountEntity.toDomain() = LedgerAccount(id, CurrencyCode.valueOf(currencyCode), LedgerAccountType.valueOf(type), LedgerAccountRole.valueOf(role), isActive)

private fun FinancialAccountRow.toDomain() = FinancialAccount(
    id = id,
    name = name,
    kind = LedgerAccountRole.valueOf(accountKind),
    currency = CurrencyCode.valueOf(currencyCode),
    institutionName = institutionName,
    provider = providerCode?.let(AccountProvider::valueOf),
    maskedAccountNumber = maskedAccountNumber,
    isArchived = isArchived,
    balanceMinor = balanceMinor,
)

private fun LedgerTransactionRow.toDomain() = LedgerTransactionSummary(
    id = id,
    operationId = operationId,
    type = LedgerTransactionType.valueOf(transactionType),
    description = description,
    currency = CurrencyCode.valueOf(currencyCode),
    occurredAt = occurredAt,
    amountMinor = amountMinor,
    isReversed = isReversed,
)

private const val millisPerDay = 86_400_000L
private val realAccountRoles = setOf(LedgerAccountRole.BANK, LedgerAccountRole.WALLET, LedgerAccountRole.CASH, LedgerAccountRole.SAVINGS_ASSET)
