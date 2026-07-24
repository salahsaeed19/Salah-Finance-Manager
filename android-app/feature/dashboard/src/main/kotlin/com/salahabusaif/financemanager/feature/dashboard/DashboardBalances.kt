package com.salahabusaif.financemanager.feature.dashboard

import com.salahabusaif.financemanager.core.model.CurrencyCode

/** Pure dashboard selection rules, kept separate so every currency path is testable. */
internal object DashboardBalances {
    fun visible(balances: Map<CurrencyCode, Long>): Map<CurrencyCode, Long> =
        balances.filterValues { it != 0L }

    fun selectedAmount(balances: Map<CurrencyCode, Long>, currency: CurrencyCode): Long =
        balances.getValue(currency)
}
