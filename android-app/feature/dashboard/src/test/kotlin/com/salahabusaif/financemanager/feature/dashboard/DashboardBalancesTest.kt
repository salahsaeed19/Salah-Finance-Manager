package com.salahabusaif.financemanager.feature.dashboard

import com.salahabusaif.financemanager.core.model.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardBalancesTest {
    private val balances = mapOf(
        CurrencyCode.ILS to 125_000L,
        CurrencyCode.USD to 200L,
        CurrencyCode.JOD to 0L,
    )

    @Test fun hidesZeroCurrencyTotals() {
        assertEquals(setOf(CurrencyCode.ILS, CurrencyCode.USD), DashboardBalances.visible(balances).keys)
    }

    @Test fun selectorUsesSeparateLedgerDerivedCurrencyTotals() {
        assertEquals(125_000L, DashboardBalances.selectedAmount(balances, CurrencyCode.ILS))
        assertEquals(200L, DashboardBalances.selectedAmount(balances, CurrencyCode.USD))
        assertEquals(0L, DashboardBalances.selectedAmount(balances, CurrencyCode.JOD))
    }

    @Test fun allZeroBalancesProduceOneEmptyDashboardState() {
        assertTrue(DashboardBalances.visible(CurrencyCode.entries.associateWith { 0L }).isEmpty())
    }
}
