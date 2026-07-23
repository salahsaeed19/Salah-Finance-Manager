package com.salahabusaif.financemanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salahabusaif.financemanager.core.database.dao.DatabaseMetadataDao
import com.salahabusaif.financemanager.core.database.entity.DatabaseMetadataEntity

/**
 * Technical foundation only. Metadata verifies Room configuration without inventing financial
 * records; the approved ledger schema is introduced in its dedicated financial-engine phase.
 */
@Database(entities = [DatabaseMetadataEntity::class], version = FinanceDatabase.VERSION, exportSchema = true)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun databaseMetadataDao(): DatabaseMetadataDao

    companion object {
        const val NAME = "salah_finance_manager.db"
        const val VERSION = 1
    }
}
