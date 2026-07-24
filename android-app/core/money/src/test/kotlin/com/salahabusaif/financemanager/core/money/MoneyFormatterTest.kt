package com.salahabusaif.financemanager.core.money

import com.salahabusaif.financemanager.core.model.CurrencyCode
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {
    @Test fun formatsZeroIls() = assertTrue(MoneyFormatter.format(Money(0, CurrencyCode.ILS), Locale.ENGLISH).isNotBlank())
    @Test fun formatsZeroUsd() = assertTrue(MoneyFormatter.format(Money(0, CurrencyCode.USD), Locale.ENGLISH).isNotBlank())
    @Test fun formatsZeroJod() = assertTrue(MoneyFormatter.format(Money(0, CurrencyCode.JOD), Locale.ENGLISH).isNotBlank())
    @Test fun formatsNegativeAndArabic() = assertNotEquals(MoneyFormatter.format(Money(-125, CurrencyCode.ILS), Locale("ar")), "")
    @Test fun wholeAmountOmitsTrailingZeroFraction() {
        assertTrue(MoneyFormatter.format(Money(125_000, CurrencyCode.ILS), Locale.ENGLISH).contains("1,250"))
        assertFalse(MoneyFormatter.format(Money(125_000, CurrencyCode.ILS), Locale.ENGLISH).contains(".00"))
    }
    @Test fun fractionalAmountRetainsMinorPrecision() = assertTrue(MoneyFormatter.format(Money(150, CurrencyCode.ILS), Locale.ENGLISH).contains("1.5"))
    @Test fun usesApprovedCompactCurrencySymbols() {
        assertTrue(MoneyFormatter.format(Money(100, CurrencyCode.ILS), Locale.ENGLISH).endsWith("₪"))
        assertTrue(MoneyFormatter.format(Money(100, CurrencyCode.USD), Locale.ENGLISH).endsWith("$"))
        assertTrue(MoneyFormatter.format(Money(100, CurrencyCode.JOD), Locale.ENGLISH).endsWith("JOD"))
        assertTrue(MoneyFormatter.format(Money(100, CurrencyCode.JOD), Locale("ar")).endsWith("د.أ"))
        assertFalse(MoneyFormatter.format(Money(100, CurrencyCode.USD), Locale.ENGLISH).contains("US"))
    }
    @Test fun rejectsCurrencyMismatch() {
        try { Money(0, CurrencyCode.ILS).requireSameCurrency(Money(0, CurrencyCode.USD)); throw AssertionError("Expected mismatch") } catch (_: IllegalArgumentException) { }
    }
}
