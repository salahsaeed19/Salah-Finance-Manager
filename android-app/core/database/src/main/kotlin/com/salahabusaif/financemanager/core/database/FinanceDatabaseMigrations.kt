package com.salahabusaif.financemanager.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FinanceDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ledger_accounts` (`id` TEXT NOT NULL, `code` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `role` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `normalBalance` TEXT NOT NULL, `isSystem` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ledger_accounts_code` ON `ledger_accounts` (`code`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_accounts_type_currencyCode` ON `ledger_accounts` (`type`, `currencyCode`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `financial_accounts` (`id` TEXT NOT NULL, `ledgerAccountId` TEXT NOT NULL, `institutionName` TEXT, `accountKind` TEXT NOT NULL, `maskedAccountNumber` TEXT, `providerCode` TEXT, `colorToken` TEXT, `iconToken` TEXT, `isArchived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`ledgerAccountId`) REFERENCES `ledger_accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_financial_accounts_ledgerAccountId` ON `financial_accounts` (`ledgerAccountId`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `transaction_groups` (`id` TEXT NOT NULL, `groupType` TEXT NOT NULL, `reference` TEXT, `notes` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_groups_groupType` ON `transaction_groups` (`groupType`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ledger_transactions` (`id` TEXT NOT NULL, `groupId` TEXT, `operationId` TEXT NOT NULL, `transactionType` TEXT NOT NULL, `status` TEXT NOT NULL, `accountingDate` INTEGER NOT NULL, `occurredAt` INTEGER, `description` TEXT NOT NULL, `notes` TEXT, `reversalOfId` TEXT, `replacedById` TEXT, `postedAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`groupId`) REFERENCES `transaction_groups`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ledger_transactions_operationId` ON `ledger_transactions` (`operationId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_transactions_groupId` ON `ledger_transactions` (`groupId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_transactions_accountingDate` ON `ledger_transactions` (`accountingDate`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_transactions_status` ON `ledger_transactions` (`status`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ledger_postings` (`id` TEXT NOT NULL, `transactionId` TEXT NOT NULL, `ledgerAccountId` TEXT NOT NULL, `direction` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `memo` TEXT, `sequence` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`transactionId`) REFERENCES `ledger_transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`ledgerAccountId`) REFERENCES `ledger_accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ledger_postings_transactionId_sequence` ON `ledger_postings` (`transactionId`, `sequence`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_postings_ledgerAccountId_transactionId` ON `ledger_postings` (`ledgerAccountId`, `transactionId`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `wallet_commission_rules` (`id` TEXT NOT NULL, `walletProvider` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `minimumMinor` INTEGER NOT NULL, `maximumMinor` INTEGER, `commissionMinor` INTEGER NOT NULL, `validFrom` INTEGER NOT NULL, `validTo` INTEGER, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_wallet_commission_rules_walletProvider_currencyCode_validFrom` ON `wallet_commission_rules` (`walletProvider`, `currencyCode`, `validFrom`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `audit_events` (`id` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `eventType` TEXT NOT NULL, `details` TEXT, `occurredAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_events_entityType_entityId` ON `audit_events` (`entityType`, `entityId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_events_occurredAt` ON `audit_events` (`occurredAt`)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `owner_profile` (`id` TEXT NOT NULL, `fullName` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `bankOfPalestineReference` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}
