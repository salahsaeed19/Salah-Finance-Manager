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
import com.salahabusaif.financemanager.core.database.entity.PersonAliasEntity
import com.salahabusaif.financemanager.core.database.entity.PersonEntity
import com.salahabusaif.financemanager.core.database.entity.PersonLedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.PersonOperationEntity
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
import com.salahabusaif.financemanager.core.ledger.Posting
import com.salahabusaif.financemanager.core.ledger.PostingDirection
import com.salahabusaif.financemanager.core.ledger.PeopleGateway
import com.salahabusaif.financemanager.core.ledger.Person
import com.salahabusaif.financemanager.core.ledger.PersonCurrencyBalance
import com.salahabusaif.financemanager.core.ledger.PersonMoneyCommand
import com.salahabusaif.financemanager.core.ledger.PersonOperation
import com.salahabusaif.financemanager.core.ledger.PersonOperationType
import com.salahabusaif.financemanager.core.ledger.PersonStatement
import com.salahabusaif.financemanager.core.ledger.PersonSummary
import com.salahabusaif.financemanager.core.ledger.PersonTransferCommand
import com.salahabusaif.financemanager.core.ledger.CreatePersonCommand
import com.salahabusaif.financemanager.core.ledger.UpdatePersonCommand
import com.salahabusaif.financemanager.core.ledger.InsufficientFundsSettlement
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
) : LedgerGateway, PeopleGateway {
    override val financialAccounts: Flow<List<FinancialAccount>> =
        database.ledgerDao().financialAccounts().map { rows -> rows.map(FinancialAccountRow::toDomain) }

    override val transactions: Flow<List<LedgerTransactionSummary>> =
        database.ledgerDao().transactionSummaries().map { rows -> rows.map(LedgerTransactionRow::toDomain) }

    override val people: Flow<List<PersonSummary>> = database.ledgerDao().people().map { rows ->
        rows.map { person -> personSummary(person) }
    }

    override suspend fun createPerson(command: CreatePersonCommand): Result<Person> = runCatching {
        val name = command.displayName.trim()
        require(name.isNotBlank()) { "Person name is required." }
        val now = System.currentTimeMillis()
        val person = PersonEntity(
            id = UUID.randomUUID().toString(),
            displayName = name,
            normalizedName = normalize(name),
            nickname = command.nickname.clean(),
            phoneNumber = command.phoneNumber.clean(),
            photoPath = command.photoPath.clean(),
            notes = command.notes.clean(),
            isArchived = false,
            createdAt = now,
            updatedAt = now,
        )
        database.withTransaction {
            val dao = database.ledgerDao()
            require(dao.person(person.id) == null)
            val aliases = command.aliases.normalizedDistinct().map { alias ->
                require(dao.aliasByNormalizedValue(normalize(alias)) == null) { "Alias already belongs to another person." }
                PersonAliasEntity(UUID.randomUUID().toString(), person.id, alias, normalize(alias), now)
            }
            dao.insertPerson(person)
            if (aliases.isNotEmpty()) dao.insertPersonAliases(aliases)
            audit("person", person.id, "CREATED", now)
        }
        person.toDomain(command.aliases.normalizedDistinct())
    }

    override suspend fun updatePerson(command: UpdatePersonCommand): Result<Unit> = runCatching {
        val name = command.displayName.trim()
        require(name.isNotBlank()) { "Person name is required." }
        val now = System.currentTimeMillis()
        database.withTransaction {
            val dao = database.ledgerDao()
            require(dao.person(command.id) != null) { "Person not found." }
            require(dao.updatePerson(command.id, name, normalize(name), command.nickname.clean(), command.phoneNumber.clean(), command.photoPath.clean(), command.notes.clean(), now) == 1)
            val newAliases = command.aliases.normalizedDistinct().map { alias ->
                val existing = dao.aliasByNormalizedValue(normalize(alias))
                require(existing == null || existing.personId == command.id) { "Alias already belongs to another person." }
                PersonAliasEntity(UUID.randomUUID().toString(), command.id, alias, normalize(alias), now)
            }
            val known = dao.aliasesForPerson(command.id).map(PersonAliasEntity::normalizedAlias).toSet()
            val additional = newAliases.filter { it.normalizedAlias !in known }
            if (additional.isNotEmpty()) dao.insertPersonAliases(additional)
            audit("person", command.id, "UPDATED", now)
        }
    }

    override suspend fun archivePerson(id: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        database.withTransaction {
            require(database.ledgerDao().archivePerson(id, true, now) == 1) { "Person not found." }
            audit("person", id, "ARCHIVED", now)
        }
    }

    override suspend fun person(id: String): Person? = database.withTransaction {
        database.ledgerDao().person(id)?.let { entity -> entity.toDomain(database.ledgerDao().aliasesForPerson(id).map(PersonAliasEntity::alias)) }
    }

    override fun personOperations(personId: String, currency: CurrencyCode): Flow<List<PersonOperation>> =
        database.ledgerDao().personOperationRows(personId, currency.isoCode).map { rows -> rows.map { it.toPersonOperation() } }

    override suspend fun deposit(command: PersonMoneyCommand): Result<Unit> = postPersonMoney(command, PersonOperationType.DEPOSIT)

    override suspend fun withdraw(command: PersonMoneyCommand): Result<Unit> = postPersonMoney(command, PersonOperationType.WITHDRAWAL)

    override suspend fun loan(command: PersonMoneyCommand): Result<Unit> = postPersonMoney(command, PersonOperationType.LOAN)

    override suspend fun repay(command: PersonMoneyCommand): Result<Unit> = postPersonMoney(command, PersonOperationType.REPAYMENT)

    override suspend fun transfer(command: PersonTransferCommand): Result<Unit> = runCatching {
        require(command.amountMinor > 0) { "Amount must be positive." }
        require(command.commissionMinor >= 0) { "Commission cannot be negative." }
        require(command.beneficiaryName.isNotBlank()) { "Beneficiary is required." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val now = System.currentTimeMillis()
            val asset = requireNotNull(dao.ledgerAccountForFinancial(command.sourceFinancialAccountId)) { "Source account not found." }.toDomain()
            require(dao.postedBalance(asset.id) >= command.amountMinor) { "Source account has insufficient funds." }
            val funds = resolvePersonAccount(command.personId, LedgerAccountRole.PERSON_FUNDS_HELD, asset.currency, now)
            val receivable = resolvePersonAccount(command.personId, LedgerAccountRole.PERSON_RECEIVABLE, asset.currency, now)
            val total = command.amountMinor + command.commissionMinor
            val held = -dao.postedBalance(funds.id)
            val fromHeld = when (command.settlement) {
                InsufficientFundsSettlement.FULL_RECEIVABLE -> 0
                else -> minOf(held.coerceAtLeast(0), total)
            }
            val fromReceivable = total - fromHeld
            require(fromReceivable == 0L || command.settlement != InsufficientFundsSettlement.REJECT) { "Person funds are insufficient." }
            val postings = buildList {
                if (fromHeld > 0) add(Posting(funds, PostingDirection.DEBIT, fromHeld))
                if (fromReceivable > 0) add(Posting(receivable, PostingDirection.DEBIT, fromReceivable))
                add(Posting(asset, PostingDirection.CREDIT, command.amountMinor))
                if (command.commissionMinor > 0) add(Posting(SystemLedgerAccounts.commissionIncome(asset.currency), PostingDirection.CREDIT, command.commissionMinor))
            }
            val transactionId = writePlan(PostingPlan(command.operationId, LedgerTransactionType.PERSON_TRANSFER, asset.currency, "Transfer for person", postings), now)
            dao.insertPersonOperation(
                PersonOperationEntity(
                    id = UUID.randomUUID().toString(),
                    personId = command.personId,
                    transactionId = transactionId,
                    operationType = PersonOperationType.TRANSFER.name,
                    financialAccountId = command.sourceFinancialAccountId,
                    currencyCode = asset.currency.isoCode,
                    amountMinor = command.amountMinor,
                    commissionMinor = command.commissionMinor,
                    fundsHeldChargedMinor = fromHeld,
                    beneficiaryName = command.beneficiaryName.trim(),
                    dueDate = null,
                    notes = command.notes.clean(),
                    createdAt = now,
                ),
            )
        }
    }

    override suspend fun statement(personId: String, currency: CurrencyCode, startAt: Long, endAt: Long): Result<PersonStatement> = runCatching {
        require(startAt <= endAt) { "Invalid statement period." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val person = requireNotNull(dao.person(personId)) { "Person not found." }
            val all = dao.personOperationRowsNow(personId, currency.isoCode).map { it.toPersonOperation() }
            val before = all.filter { it.occurredAt < startAt }
            val period = all.filter { it.occurredAt in startAt..endAt }
            val openingHeld = before.sumOf { it.fundsHeldDelta() }
            val openingReceivable = before.sumOf { it.receivableDelta() }
            val closingHeld = openingHeld + period.sumOf { it.fundsHeldDelta() }
            val closingReceivable = openingReceivable + period.sumOf { it.receivableDelta() }
            PersonStatement(person.toDomain(dao.aliasesForPerson(personId).map(PersonAliasEntity::alias)), currency, openingHeld, openingReceivable, period, closingHeld, closingReceivable)
        }
    }

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

    private suspend fun postPersonMoney(command: PersonMoneyCommand, type: PersonOperationType): Result<Unit> = runCatching {
        require(command.amountMinor > 0) { "Amount must be positive." }
        database.withTransaction {
            val dao = database.ledgerDao()
            val now = System.currentTimeMillis()
            val asset = requireNotNull(dao.ledgerAccountForFinancial(command.financialAccountId)) { "Financial account not found." }.toDomain()
            val funds = resolvePersonAccount(command.personId, LedgerAccountRole.PERSON_FUNDS_HELD, asset.currency, now)
            val receivable = resolvePersonAccount(command.personId, LedgerAccountRole.PERSON_RECEIVABLE, asset.currency, now)
            val plan = when (type) {
                PersonOperationType.DEPOSIT -> PostingPlan(command.operationId, LedgerTransactionType.PERSON_DEPOSIT, asset.currency, "Person deposit", listOf(Posting(asset, PostingDirection.DEBIT, command.amountMinor), Posting(funds, PostingDirection.CREDIT, command.amountMinor)))
                PersonOperationType.WITHDRAWAL -> {
                    require(-dao.postedBalance(funds.id) >= command.amountMinor) { "Withdrawal exceeds money held." }
                    PostingPlan(command.operationId, LedgerTransactionType.PERSON_WITHDRAWAL, asset.currency, "Person withdrawal", listOf(Posting(funds, PostingDirection.DEBIT, command.amountMinor), Posting(asset, PostingDirection.CREDIT, command.amountMinor)))
                }
                PersonOperationType.LOAN -> {
                    require(dao.postedBalance(asset.id) >= command.amountMinor) { "Source account has insufficient funds." }
                    PostingPlan(command.operationId, LedgerTransactionType.PERSON_LOAN, asset.currency, "Loan to person", listOf(Posting(receivable, PostingDirection.DEBIT, command.amountMinor), Posting(asset, PostingDirection.CREDIT, command.amountMinor)))
                }
                PersonOperationType.REPAYMENT -> {
                    require(dao.postedBalance(receivable.id) >= command.amountMinor) { "Repayment exceeds the amount owed." }
                    PostingPlan(command.operationId, LedgerTransactionType.PERSON_REPAYMENT, asset.currency, "Person repayment", listOf(Posting(asset, PostingDirection.DEBIT, command.amountMinor), Posting(receivable, PostingDirection.CREDIT, command.amountMinor)))
                }
                PersonOperationType.TRANSFER -> error("Use transfer command.")
            }
            require(PostingEngine.validate(plan) is LedgerValidationResult.Valid)
            val transactionId = writePlan(plan, now)
            dao.insertPersonOperation(
                PersonOperationEntity(
                    id = UUID.randomUUID().toString(),
                    personId = command.personId,
                    transactionId = transactionId,
                    operationType = type.name,
                    financialAccountId = command.financialAccountId,
                    currencyCode = asset.currency.isoCode,
                    amountMinor = command.amountMinor,
                    commissionMinor = 0,
                    fundsHeldChargedMinor = when (type) {
                        PersonOperationType.DEPOSIT -> command.amountMinor
                        PersonOperationType.WITHDRAWAL -> command.amountMinor
                        else -> 0
                    },
                    beneficiaryName = null,
                    dueDate = command.dueDate,
                    notes = command.notes.clean(),
                    createdAt = now,
                ),
            )
        }
    }

    private suspend fun resolvePersonAccount(personId: String, role: LedgerAccountRole, currency: CurrencyCode, now: Long): LedgerAccount {
        val dao = database.ledgerDao()
        require(dao.person(personId) != null) { "Person not found." }
        val existing = dao.personLedgerAccount(personId, role.name, currency.isoCode)
        if (existing != null) return requireNotNull(dao.account(existing.ledgerAccountId)).toDomain()
        val type = if (role == LedgerAccountRole.PERSON_FUNDS_HELD) LedgerAccountType.LIABILITY else LedgerAccountType.ASSET
        val account = LedgerAccount("person-$personId-${role.name.lowercase()}-${currency.isoCode}", currency, type, role)
        dao.insertAccounts(listOf(account.toEntity("Person ${role.name} ${currency.isoCode}", now)))
        dao.insertPersonLedgerAccount(PersonLedgerAccountEntity(UUID.randomUUID().toString(), personId, account.id, role.name, currency.isoCode, now))
        return account
    }

    private suspend fun personSummary(person: PersonEntity): PersonSummary {
        val dao = database.ledgerDao()
        val balances = CurrencyCode.entries.map { currency ->
            val heldAccount = dao.personLedgerAccount(person.id, LedgerAccountRole.PERSON_FUNDS_HELD.name, currency.isoCode)
            val receivableAccount = dao.personLedgerAccount(person.id, LedgerAccountRole.PERSON_RECEIVABLE.name, currency.isoCode)
            PersonCurrencyBalance(currency, heldAccount?.let { -dao.postedBalance(it.ledgerAccountId) } ?: 0, receivableAccount?.let { dao.postedBalance(it.ledgerAccountId) } ?: 0)
        }
        return PersonSummary(person.toDomain(dao.aliasesForPerson(person.id).map(PersonAliasEntity::alias)), balances)
    }

    private suspend fun writePlan(plan: PostingPlan, now: Long, groupId: String? = null): String {
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
        return transactionId
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

    fun commissionIncome(currency: CurrencyCode) = systemAccount("commission-income", currency, LedgerAccountType.INCOME, LedgerAccountRole.COMMISSION_INCOME)

    fun expense(currency: CurrencyCode) = systemAccount("expense", currency, LedgerAccountType.EXPENSE, LedgerAccountRole.EXPENSE_CATEGORY)

    fun fxClearing(currency: CurrencyCode) = systemAccount("fx-clearing", currency, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING)

    fun entities(now: Long): List<LedgerAccountEntity> = CurrencyCode.entries.flatMap { currency ->
        listOf(
            openingEquity(currency),
            salaryIncome(currency),
            otherIncome(currency),
            commissionIncome(currency),
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

private fun PersonEntity.toDomain(aliases: List<String>) = Person(id, displayName, nickname, phoneNumber, photoPath, notes, aliases, isArchived)

private fun com.salahabusaif.financemanager.core.database.dao.PersonOperationRow.toPersonOperation() = PersonOperation(
    id = id,
    transactionId = transactionId,
    type = PersonOperationType.valueOf(operationType),
    currency = CurrencyCode.valueOf(currencyCode),
    amountMinor = amountMinor,
    commissionMinor = commissionMinor,
    fundsHeldChargedMinor = fundsHeldChargedMinor,
    financialAccountId = financialAccountId,
    beneficiaryName = beneficiaryName,
    dueDate = dueDate,
    notes = notes,
    occurredAt = occurredAt,
)

private fun PersonOperation.fundsHeldDelta(): Long = when (type) {
    PersonOperationType.DEPOSIT -> amountMinor
    PersonOperationType.WITHDRAWAL -> -amountMinor
    PersonOperationType.TRANSFER -> -fundsHeldChargedMinor
    else -> 0
}

private fun PersonOperation.receivableDelta(): Long = when (type) {
    PersonOperationType.LOAN -> amountMinor
    PersonOperationType.REPAYMENT -> -amountMinor
    PersonOperationType.TRANSFER -> amountMinor + commissionMinor - fundsHeldChargedMinor
    else -> 0
}

private fun String?.clean(): String? = this?.trim()?.takeIf(String::isNotBlank)

private fun List<String>.normalizedDistinct(): List<String> = map(String::trim).filter(String::isNotBlank).distinctBy(::normalize)

private fun normalize(value: String): String = value.trim().lowercase()

private const val millisPerDay = 86_400_000L
private val realAccountRoles = setOf(LedgerAccountRole.BANK, LedgerAccountRole.WALLET, LedgerAccountRole.CASH, LedgerAccountRole.SAVINGS_ASSET)
