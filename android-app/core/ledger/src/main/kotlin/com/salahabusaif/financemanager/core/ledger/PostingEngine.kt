package com.salahabusaif.financemanager.core.ledger

import com.salahabusaif.financemanager.core.model.CurrencyCode

object PostingEngine {
    fun validate(plan: PostingPlan): LedgerValidationResult {
        if (plan.operationId.isBlank()) return LedgerValidationResult.Invalid("Operation ID is required.")
        if (plan.postings.size < 2) return LedgerValidationResult.Invalid("At least two postings are required.")
        if (plan.postings.any { !it.account.isActive }) return LedgerValidationResult.Invalid("Inactive account.")
        if (plan.postings.any { it.account.currency != plan.currency }) {
            return LedgerValidationResult.Invalid("Account currency mismatch.")
        }
        val debit = plan.postings.filter { it.direction == PostingDirection.DEBIT }.sumOf { it.amountMinor }
        val credit = plan.postings.filter { it.direction == PostingDirection.CREDIT }.sumOf { it.amountMinor }
        return if (debit == credit) LedgerValidationResult.Valid else LedgerValidationResult.Invalid("Debits and credits must balance.")
    }

    fun openingBalance(
        operationId: String,
        asset: LedgerAccount,
        equity: LedgerAccount,
        amountMinor: Long,
    ): PostingPlan = plan(operationId, LedgerTransactionType.OPENING_BALANCE, asset.currency, "Opening balance", listOf(debit(asset, amountMinor), credit(equity, amountMinor)))

    fun personalIncome(
        operationId: String,
        destination: LedgerAccount,
        income: LedgerAccount,
        amountMinor: Long,
        salary: Boolean,
    ): PostingPlan = plan(operationId, if (salary) LedgerTransactionType.SALARY_INCOME else LedgerTransactionType.OTHER_INCOME, destination.currency, "Personal income", listOf(debit(destination, amountMinor), credit(income, amountMinor)))

    fun personalExpense(
        operationId: String,
        expense: LedgerAccount,
        source: LedgerAccount,
        amountMinor: Long,
    ): PostingPlan = plan(operationId, LedgerTransactionType.PERSONAL_EXPENSE, source.currency, "Personal expense", listOf(debit(expense, amountMinor), credit(source, amountMinor)))

    fun internalTransfer(
        operationId: String,
        source: LedgerAccount,
        destination: LedgerAccount,
        amountMinor: Long,
    ): PostingPlan {
        require(source.id != destination.id) { "Transfer accounts must differ." }
        require(source.currency == destination.currency) { "Transfer currencies must match." }
        return plan(operationId, LedgerTransactionType.INTERNAL_TRANSFER, source.currency, "Internal transfer", listOf(debit(destination, amountMinor), credit(source, amountMinor)))
    }

    fun reversal(operationId: String, original: PostingPlan): PostingPlan =
        plan(
            operationId = operationId,
            type = LedgerTransactionType.REVERSAL,
            currency = original.currency,
            description = "Reversal: ${original.description}",
            postings = original.postings.map { posting -> posting.copy(direction = posting.direction.opposite()) },
            reversalOfId = original.operationId,
        )

    fun currencyExchange(
        operationId: String,
        source: LedgerAccount,
        sourceClearing: LedgerAccount,
        destinationClearing: LedgerAccount,
        destination: LedgerAccount,
        sourceAmountMinor: Long,
        destinationAmountMinor: Long,
        description: String,
    ): CurrencyExchangePlan {
        require(source.currency != destination.currency) { "Currency exchange requires two currencies." }
        return CurrencyExchangePlan(
            groupOperationId = operationId,
            sourcePlan = plan(
                "$operationId-source",
                LedgerTransactionType.CURRENCY_EXCHANGE,
                source.currency,
                description,
                listOf(debit(sourceClearing, sourceAmountMinor), credit(source, sourceAmountMinor)),
            ),
            destinationPlan = plan(
                "$operationId-destination",
                LedgerTransactionType.CURRENCY_EXCHANGE,
                destination.currency,
                description,
                listOf(debit(destination, destinationAmountMinor), credit(destinationClearing, destinationAmountMinor)),
            ),
            description = description,
        )
    }

    private fun plan(
        operationId: String,
        type: LedgerTransactionType,
        currency: CurrencyCode,
        description: String,
        postings: List<Posting>,
        reversalOfId: String? = null,
    ): PostingPlan = PostingPlan(operationId, type, currency, description, postings, reversalOfId = reversalOfId).also {
        check(validate(it) is LedgerValidationResult.Valid) { "Invalid posting template." }
    }

    private fun debit(account: LedgerAccount, amountMinor: Long) = Posting(account, PostingDirection.DEBIT, amountMinor)

    private fun credit(account: LedgerAccount, amountMinor: Long) = Posting(account, PostingDirection.CREDIT, amountMinor)
}

object CommissionCalculator {
    fun commissionFor(amountMinor: Long, rules: List<CommissionRule>, at: Long): Long? {
        require(amountMinor > 0) { "Commission amount must be positive." }
        return rules
            .filter { rule ->
                rule.minimumMinor <= amountMinor &&
                    (rule.maximumMinor == null || amountMinor <= rule.maximumMinor) &&
                    rule.validFrom <= at &&
                    (rule.validTo == null || at <= rule.validTo)
            }.maxByOrNull(CommissionRule::validFrom)
            ?.commissionMinor
    }
}

private fun PostingDirection.opposite() = if (this == PostingDirection.DEBIT) PostingDirection.CREDIT else PostingDirection.DEBIT
