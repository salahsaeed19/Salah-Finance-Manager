package com.salahabusaif.financemanager.core.ledger

import com.salahabusaif.financemanager.core.model.CurrencyCode

enum class LedgerAccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    INCOME,
    EXPENSE,
    CLEARING,
}

enum class LedgerAccountRole {
    BANK,
    WALLET,
    CASH,
    SAVINGS_ASSET,
    SALARY_INCOME,
    OTHER_INCOME,
    COMMISSION_INCOME,
    EXPENSE_CATEGORY,
    FX_CLEARING,
    TRANSFER_CLEARING,
    OPENING_BALANCE_EQUITY,
    PERSONAL_PAYABLE,
    SUSPENSE,
}

enum class AccountProvider {
    BANK_OF_PALESTINE,
    JAWWAL_PAY,
    PALPAY,
    CASH,
}

enum class NormalBalance {
    DEBIT,
    CREDIT,
}

enum class PostingDirection {
    DEBIT,
    CREDIT,
}

enum class LedgerTransactionStatus {
    DRAFT,
    POSTED,
    REVERSED,
    PENDING_REVIEW,
    RECONCILED,
    EXCLUDED,
    FAILED,
}

enum class LedgerTransactionType {
    OPENING_BALANCE,
    SALARY_INCOME,
    OTHER_INCOME,
    PERSONAL_EXPENSE,
    INTERNAL_TRANSFER,
    CURRENCY_EXCHANGE,
    COMMISSION_INCOME,
    REVERSAL,
    REPLACEMENT,
}

data class LedgerAccount(
    val id: String,
    val currency: CurrencyCode,
    val type: LedgerAccountType,
    val role: LedgerAccountRole,
    val isActive: Boolean = true,
)

data class Posting(
    val account: LedgerAccount,
    val direction: PostingDirection,
    val amountMinor: Long,
    val memo: String? = null,
) {
    init {
        require(amountMinor > 0) { "Posting amount must be positive." }
    }
}

data class PostingPlan(
    val operationId: String,
    val type: LedgerTransactionType,
    val currency: CurrencyCode,
    val description: String,
    val postings: List<Posting>,
    val reversalOfId: String? = null,
    val replacementOfId: String? = null,
)

data class CurrencyExchangePlan(
    val groupOperationId: String,
    val sourcePlan: PostingPlan,
    val destinationPlan: PostingPlan,
    val description: String,
)

data class CommissionRule(
    val walletProvider: String,
    val currency: CurrencyCode,
    val minimumMinor: Long,
    val maximumMinor: Long? = null,
    val commissionMinor: Long,
    val validFrom: Long,
    val validTo: Long? = null,
)

sealed interface LedgerValidationResult {
    data object Valid : LedgerValidationResult

    data class Invalid(val reason: String) : LedgerValidationResult
}
