package com.salahabusaif.financemanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salahabusaif.financemanager.core.database.dao.DatabaseMetadataDao
import com.salahabusaif.financemanager.core.database.dao.LedgerDao
import com.salahabusaif.financemanager.core.database.entity.AuditEventEntity
import com.salahabusaif.financemanager.core.database.entity.DatabaseMetadataEntity
import com.salahabusaif.financemanager.core.database.entity.FinancialAccountEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerPostingEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerTransactionEntity
import com.salahabusaif.financemanager.core.database.entity.OwnerProfileEntity
import com.salahabusaif.financemanager.core.database.entity.PersonAliasEntity
import com.salahabusaif.financemanager.core.database.entity.PersonEntity
import com.salahabusaif.financemanager.core.database.entity.PersonLedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.PersonOperationEntity
import com.salahabusaif.financemanager.core.database.entity.TransactionGroupEntity
import com.salahabusaif.financemanager.core.database.entity.WalletCommissionRuleEntity

/**
 * Technical foundation only. Metadata verifies Room configuration without inventing financial
 * records; the approved ledger schema is introduced in its dedicated financial-engine phase.
 */
@Database(
    entities = [
        DatabaseMetadataEntity::class,
        LedgerAccountEntity::class,
        FinancialAccountEntity::class,
        OwnerProfileEntity::class,
        PersonEntity::class,
        PersonAliasEntity::class,
        PersonLedgerAccountEntity::class,
        PersonOperationEntity::class,
        TransactionGroupEntity::class,
        LedgerTransactionEntity::class,
        LedgerPostingEntity::class,
        WalletCommissionRuleEntity::class,
        AuditEventEntity::class,
    ],
    version = FinanceDatabase.VERSION,
    exportSchema = true,
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun databaseMetadataDao(): DatabaseMetadataDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        const val NAME = "salah_finance_manager.db"
        const val VERSION = 5
    }
}
