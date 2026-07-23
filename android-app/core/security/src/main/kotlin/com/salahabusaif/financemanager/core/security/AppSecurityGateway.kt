package com.salahabusaif.financemanager.core.security

/** Foundation only; PIN, biometrics, and encrypted backups are intentionally future work. */
interface AppSecurityGateway {
    suspend fun isProtectionConfigured(): Boolean
}
