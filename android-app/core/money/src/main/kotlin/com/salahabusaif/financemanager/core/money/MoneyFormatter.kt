package com.salahabusaif.financemanager.core.money

import com.salahabusaif.financemanager.core.model.CurrencyCode
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {
    fun format(money: Money, locale: Locale): String {
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = money.currency.fractionDigits
            minimumFractionDigits = 0
        }
        val value = BigDecimal.valueOf(money.minorUnits).movePointLeft(money.currency.fractionDigits)
        return "${formatter.format(value)}\u00A0${symbolFor(money.currency, locale)}"
    }

    /** Uses stable, compact symbols instead of locale-dependent variants such as \"US$\". */
    fun symbolFor(currency: CurrencyCode, locale: Locale): String = when (currency) {
        CurrencyCode.ILS -> "₪"
        CurrencyCode.USD -> "$"
        CurrencyCode.JOD -> if (locale.language == "ar") "د.أ" else "JOD"
    }
}
