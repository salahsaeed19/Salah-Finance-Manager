package com.salahabusaif.financemanager.core.money

import com.salahabusaif.financemanager.core.model.CurrencyCode

data class Money(val minorUnits: Long, val currency: CurrencyCode) {
    fun requireSameCurrency(other: Money) {
        require(currency == other.currency) { "Currency mismatch: $currency and ${other.currency}" }
    }
}
