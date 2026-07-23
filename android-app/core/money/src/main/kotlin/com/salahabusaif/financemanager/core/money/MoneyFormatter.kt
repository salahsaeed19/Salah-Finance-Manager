package com.salahabusaif.financemanager.core.money

import com.salahabusaif.financemanager.core.model.CurrencyCode
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    fun format(money: Money, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(money.currency.isoCode)
            maximumFractionDigits = money.currency.fractionDigits
            minimumFractionDigits = money.currency.fractionDigits
        }
        val value = BigDecimal.valueOf(money.minorUnits).movePointLeft(money.currency.fractionDigits)
        return formatter.format(value)
    }
}
