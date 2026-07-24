package com.salahabusaif.financemanager.core.ledger

import com.salahabusaif.financemanager.core.model.CurrencyCode
import kotlinx.coroutines.flow.Flow

data class FinancialAccount(
    val id: String,
    val name: String,
    val kind: LedgerAccountRole,
    val currency: CurrencyCode,
    val institutionName: String? = null,
    val provider: AccountProvider? = null,
    val maskedAccountNumber: String? = null,
    val isArchived: Boolean = false,
    val balanceMinor: Long = 0,
)

data class CreateFinancialAccountCommand(
    val operationId: String,
    val name: String,
    val kind: LedgerAccountRole,
    val currency: CurrencyCode,
    val institutionName: String? = null,
    val provider: AccountProvider? = null,
    val maskedAccountNumber: String? = null,
    val openingBalanceMinor: Long = 0,
)

data class UpdateFinancialAccountCommand(
    val id: String,
    val name: String,
    val institutionName: String? = null,
    val maskedAccountNumber: String? = null,
)

data class LedgerTransactionSummary(
    val id: String,
    val operationId: String,
    val type: LedgerTransactionType,
    val description: String,
    val currency: CurrencyCode,
    val occurredAt: Long,
    val amountMinor: Long,
    val isReversed: Boolean,
)

data class RecordTransactionCommand(
    val operationId: String,
    val financialAccountId: String,
    val amountMinor: Long,
    val description: String,
    val type: LedgerTransactionType,
)

data class TransferCommand(
    val operationId: String,
    val sourceFinancialAccountId: String,
    val destinationFinancialAccountId: String,
    val amountMinor: Long,
    val description: String,
)

data class ExchangeCommand(
    val operationId: String,
    val sourceFinancialAccountId: String,
    val destinationFinancialAccountId: String,
    val sourceAmountMinor: Long,
    val destinationAmountMinor: Long,
    val description: String,
)

interface LedgerGateway {
    val financialAccounts: Flow<List<FinancialAccount>>

    val transactions: Flow<List<LedgerTransactionSummary>>

    suspend fun createFinancialAccount(command: CreateFinancialAccountCommand): Result<FinancialAccount>

    suspend fun updateFinancialAccount(command: UpdateFinancialAccountCommand): Result<Unit>

    suspend fun archiveFinancialAccount(id: String): Result<Unit>

    suspend fun post(plan: PostingPlan): Result<Unit>

    suspend fun recordPersonalTransaction(command: RecordTransactionCommand): Result<Unit>

    suspend fun recordTransfer(command: TransferCommand): Result<Unit>

    suspend fun recordExchange(command: ExchangeCommand): Result<Unit>

    suspend fun postExchange(plan: CurrencyExchangePlan): Result<Unit>

    suspend fun reverse(transactionId: String, operationId: String): Result<Unit>
}
