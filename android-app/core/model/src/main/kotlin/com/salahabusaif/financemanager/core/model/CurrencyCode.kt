package com.salahabusaif.financemanager.core.model

enum class CurrencyCode(val isoCode: String, val fractionDigits: Int) {
    ILS("ILS", 2),
    USD("USD", 2),
    JOD("JOD", 3),
}
