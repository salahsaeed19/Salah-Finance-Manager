package com.salahabusaif.financemanager.core.ledger

import com.salahabusaif.financemanager.core.model.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostingEngineTest {
    private val bank = account("bank", CurrencyCode.ILS, LedgerAccountType.ASSET, LedgerAccountRole.BANK)
    private val salary = account("salary", CurrencyCode.ILS, LedgerAccountType.INCOME, LedgerAccountRole.SALARY_INCOME)
    private val expense = account("expense", CurrencyCode.ILS, LedgerAccountType.EXPENSE, LedgerAccountRole.EXPENSE_CATEGORY)
    private val wallet = account("wallet", CurrencyCode.ILS, LedgerAccountType.ASSET, LedgerAccountRole.WALLET)

    @Test
    fun salaryIncomeIsBalanced() {
        val plan = PostingEngine.personalIncome("income-1", bank, salary, 120_000, salary = true)

        assertTrue(PostingEngine.validate(plan) is LedgerValidationResult.Valid)
        assertEquals(PostingDirection.DEBIT, plan.postings.first().direction)
    }

    @Test
    fun transferRejectsCurrencyMismatch() {
        val usdWallet = account("usd-wallet", CurrencyCode.USD, LedgerAccountType.ASSET, LedgerAccountRole.WALLET)

        runCatching { PostingEngine.internalTransfer("transfer-1", bank, usdWallet, 100) }
            .onSuccess { throw AssertionError("Expected currency mismatch") }
    }

    @Test
    fun reversalUsesOppositePostings() {
        val original = PostingEngine.personalExpense("expense-1", expense, wallet, 5_000)
        val reversal = PostingEngine.reversal("reversal-1", original)

        assertTrue(PostingEngine.validate(reversal) is LedgerValidationResult.Valid)
        assertEquals(PostingDirection.CREDIT, reversal.postings.first().direction)
    }

    @Test
    fun currencyExchangeCreatesOneBalancedTransactionPerCurrency() {
        val usdWallet = account("usd-wallet", CurrencyCode.USD, LedgerAccountType.ASSET, LedgerAccountRole.WALLET)
        val ilsClearing = account("ils-clearing", CurrencyCode.ILS, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING)
        val usdClearing = account("usd-clearing", CurrencyCode.USD, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING)

        val exchange = PostingEngine.currencyExchange("fx-1", wallet, ilsClearing, usdClearing, usdWallet, 10_000, 2_700, "Exchange")

        assertTrue(PostingEngine.validate(exchange.sourcePlan) is LedgerValidationResult.Valid)
        assertTrue(PostingEngine.validate(exchange.destinationPlan) is LedgerValidationResult.Valid)
    }

    @Test
    fun jodToIlsExchangeKeepsTheCurrenciesInSeparateBalancedPlans() {
        val jodBank = account("jod-bank", CurrencyCode.JOD, LedgerAccountType.ASSET, LedgerAccountRole.BANK)
        val ilsBank = account("ils-bank", CurrencyCode.ILS, LedgerAccountType.ASSET, LedgerAccountRole.BANK)
        val plan = PostingEngine.currencyExchange(
            "jod-ils",
            jodBank,
            account("jod-clearing", CurrencyCode.JOD, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING),
            account("ils-clearing", CurrencyCode.ILS, LedgerAccountType.CLEARING, LedgerAccountRole.FX_CLEARING),
            ilsBank,
            100,
            500,
            "JOD to ILS",
        )

        assertEquals(CurrencyCode.JOD, plan.sourcePlan.currency)
        assertEquals(CurrencyCode.ILS, plan.destinationPlan.currency)
        assertTrue(PostingEngine.validate(plan.sourcePlan) is LedgerValidationResult.Valid)
        assertTrue(PostingEngine.validate(plan.destinationPlan) is LedgerValidationResult.Valid)
    }

    @Test
    fun commissionRuleRespectsDatesAndRange() {
        val rule = CommissionRule("wallet", CurrencyCode.ILS, 100, 1_000, 25, validFrom = 10)

        assertEquals(25L, CommissionCalculator.commissionFor(500, listOf(rule), 10))
        assertNull(CommissionCalculator.commissionFor(1_001, listOf(rule), 10))
    }

    private fun account(
        id: String,
        currency: CurrencyCode,
        type: LedgerAccountType,
        role: LedgerAccountRole,
    ) = LedgerAccount(id, currency, type, role)
}
