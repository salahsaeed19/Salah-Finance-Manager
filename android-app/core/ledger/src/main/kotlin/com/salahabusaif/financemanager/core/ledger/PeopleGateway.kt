package com.salahabusaif.financemanager.core.ledger

import com.salahabusaif.financemanager.core.model.CurrencyCode
import kotlinx.coroutines.flow.Flow

data class Person(
    val id: String,
    val displayName: String,
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val photoPath: String? = null,
    val notes: String? = null,
    val aliases: List<String> = emptyList(),
    val isArchived: Boolean = false,
)

data class PersonCurrencyBalance(
    val currency: CurrencyCode,
    val fundsHeldMinor: Long,
    val receivableMinor: Long,
)

data class PersonSummary(
    val person: Person,
    val balances: List<PersonCurrencyBalance>,
    val lastActivityAt: Long? = null,
)

data class PersonOperation(
    val id: String,
    val transactionId: String,
    val type: PersonOperationType,
    val currency: CurrencyCode,
    val amountMinor: Long,
    val commissionMinor: Long,
    val fundsHeldChargedMinor: Long,
    val financialAccountId: String?,
    val beneficiaryName: String?,
    val dueDate: Long?,
    val notes: String?,
    val occurredAt: Long,
)

enum class PersonOperationType { DEPOSIT, WITHDRAWAL, LOAN, REPAYMENT, TRANSFER }

enum class InsufficientFundsSettlement { REJECT, AVAILABLE_FUNDS_AND_RECEIVABLE, FULL_RECEIVABLE }

data class CreatePersonCommand(
    val displayName: String,
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val aliases: List<String> = emptyList(),
    val photoPath: String? = null,
    val notes: String? = null,
)

data class UpdatePersonCommand(
    val id: String,
    val displayName: String,
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val aliases: List<String> = emptyList(),
    val photoPath: String? = null,
    val notes: String? = null,
)

data class PersonMoneyCommand(
    val operationId: String,
    val personId: String,
    val financialAccountId: String,
    val amountMinor: Long,
    val notes: String? = null,
    val dueDate: Long? = null,
)

data class PersonTransferCommand(
    val operationId: String,
    val personId: String,
    val sourceFinancialAccountId: String,
    val amountMinor: Long,
    val commissionMinor: Long = 0,
    val beneficiaryName: String,
    val notes: String? = null,
    val settlement: InsufficientFundsSettlement = InsufficientFundsSettlement.REJECT,
)

data class PersonStatement(
    val person: Person,
    val currency: CurrencyCode,
    val openingFundsHeldMinor: Long,
    val openingReceivableMinor: Long,
    val operations: List<PersonOperation>,
    val closingFundsHeldMinor: Long,
    val closingReceivableMinor: Long,
)

interface PeopleGateway {
    val people: Flow<List<PersonSummary>>

    suspend fun createPerson(command: CreatePersonCommand): Result<Person>

    suspend fun updatePerson(command: UpdatePersonCommand): Result<Unit>

    suspend fun archivePerson(id: String): Result<Unit>

    suspend fun person(id: String): Person?

    fun personOperations(personId: String, currency: CurrencyCode): Flow<List<PersonOperation>>

    suspend fun deposit(command: PersonMoneyCommand): Result<Unit>

    suspend fun withdraw(command: PersonMoneyCommand): Result<Unit>

    suspend fun loan(command: PersonMoneyCommand): Result<Unit>

    suspend fun repay(command: PersonMoneyCommand): Result<Unit>

    suspend fun transfer(command: PersonTransferCommand): Result<Unit>

    suspend fun statement(personId: String, currency: CurrencyCode, startAt: Long, endAt: Long): Result<PersonStatement>
}
