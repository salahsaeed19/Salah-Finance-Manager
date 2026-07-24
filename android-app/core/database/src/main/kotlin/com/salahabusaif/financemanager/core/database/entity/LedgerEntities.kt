package com.salahabusaif.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_accounts",
    indices = [Index(value = ["code"], unique = true), Index(value = ["type", "currencyCode"])],
)
data class LedgerAccountEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val type: String,
    val role: String,
    val currencyCode: String,
    val normalBalance: String,
    val isSystem: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "financial_accounts",
    foreignKeys = [ForeignKey(entity = LedgerAccountEntity::class, parentColumns = ["id"], childColumns = ["ledgerAccountId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["ledgerAccountId"], unique = true)],
)
data class FinancialAccountEntity(
    @PrimaryKey val id: String,
    val ledgerAccountId: String,
    val institutionName: String?,
    val accountKind: String,
    val maskedAccountNumber: String?,
    val providerCode: String?,
    val colorToken: String?,
    val iconToken: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "owner_profile")
data class OwnerProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phoneNumber: String,
    val bankOfPalestineReference: String,
    val updatedAt: Long,
)

@Entity(tableName = "people", indices = [Index(value = ["normalizedName"]), Index(value = ["isArchived"])])
data class PersonEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val normalizedName: String,
    val nickname: String?,
    val phoneNumber: String?,
    val photoPath: String?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "person_aliases",
    foreignKeys = [ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["personId"]), Index(value = ["normalizedAlias"], unique = true)],
)
data class PersonAliasEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val alias: String,
    val normalizedAlias: String,
    val createdAt: Long,
)

@Entity(
    tableName = "person_ledger_accounts",
    foreignKeys = [
        ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = LedgerAccountEntity::class, parentColumns = ["id"], childColumns = ["ledgerAccountId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["ledgerAccountId"], unique = true), Index(value = ["personId", "role", "currencyCode"], unique = true)],
)
data class PersonLedgerAccountEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val ledgerAccountId: String,
    val role: String,
    val currencyCode: String,
    val createdAt: Long,
)

@Entity(
    tableName = "person_operations",
    foreignKeys = [
        ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = LedgerTransactionEntity::class, parentColumns = ["id"], childColumns = ["transactionId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["personId", "currencyCode"]), Index(value = ["transactionId"], unique = true)],
)
data class PersonOperationEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val transactionId: String,
    val operationType: String,
    val financialAccountId: String?,
    val currencyCode: String,
    val amountMinor: Long,
    val commissionMinor: Long,
    val fundsHeldChargedMinor: Long,
    val beneficiaryName: String?,
    val dueDate: Long?,
    val notes: String?,
    val createdAt: Long,
)

@Entity(tableName = "transaction_groups", indices = [Index(value = ["groupType"])])
data class TransactionGroupEntity(
    @PrimaryKey val id: String,
    val groupType: String,
    val reference: String?,
    val notes: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [ForeignKey(entity = TransactionGroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["groupId"]), Index(value = ["accountingDate"]), Index(value = ["status"]), Index(value = ["operationId"], unique = true)],
)
data class LedgerTransactionEntity(
    @PrimaryKey val id: String,
    val groupId: String?,
    val operationId: String,
    val transactionType: String,
    val status: String,
    val accountingDate: Long,
    val occurredAt: Long?,
    val description: String,
    val notes: String?,
    val reversalOfId: String?,
    val replacedById: String?,
    val postedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val version: Long,
)

@Entity(
    tableName = "ledger_postings",
    foreignKeys = [
        ForeignKey(entity = LedgerTransactionEntity::class, parentColumns = ["id"], childColumns = ["transactionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = LedgerAccountEntity::class, parentColumns = ["id"], childColumns = ["ledgerAccountId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["transactionId", "sequence"], unique = true), Index(value = ["ledgerAccountId", "transactionId"])],
)
data class LedgerPostingEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val ledgerAccountId: String,
    val direction: String,
    val amountMinor: Long,
    val currencyCode: String,
    val memo: String?,
    val sequence: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "wallet_commission_rules",
    indices = [Index(value = ["walletProvider", "currencyCode", "validFrom"], unique = true)],
)
data class WalletCommissionRuleEntity(
    @PrimaryKey val id: String,
    val walletProvider: String,
    val currencyCode: String,
    val minimumMinor: Long,
    val maximumMinor: Long?,
    val commissionMinor: Long,
    val validFrom: Long,
    val validTo: Long?,
    val isActive: Boolean,
    val createdAt: Long,
)

@Entity(tableName = "audit_events", indices = [Index(value = ["entityType", "entityId"]), Index(value = ["occurredAt"])])
data class AuditEventEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val eventType: String,
    val details: String?,
    val occurredAt: Long,
)
