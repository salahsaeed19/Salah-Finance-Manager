package com.salahabusaif.financemanager.core.money

import com.salahabusaif.financemanager.core.model.CurrencyCode
import java.util.Locale
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {
    @Test fun formatsZeroIls() = assertTrue(MoneyFormatter.format(Money(0, CurrencyCode.ILS), Locale.ENGLISH).isNotBlank())
    @Test fun formatsZeroUsd() = assertTrue(MoneyFormatter.format(Money(0, CurrencyCode.USD), Locale.ENGLISH).isNotBlank())
    @Test fun formatsNegativeAndArabic() = assertNotEquals(MoneyFormatter.format(Money(-125, CurrencyCode.ILS), Locale("ar")), "")
    @Test fun rejectsCurrencyMismatch() {
        try { Money(0, CurrencyCode.ILS).requireSameCurrency(Money(0, CurrencyCode.USD)); throw AssertionError("Expected mismatch") } catch (_: IllegalArgumentException) { }
    }
}
