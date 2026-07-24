package com.salahabusaif.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.salahabusaif.financemanager.core.database.entity.LedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.AuditEventEntity
import com.salahabusaif.financemanager.core.database.entity.FinancialAccountEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerPostingEntity
import com.salahabusaif.financemanager.core.database.entity.LedgerTransactionEntity
import com.salahabusaif.financemanager.core.database.entity.OwnerProfileEntity
import com.salahabusaif.financemanager.core.database.entity.PersonAliasEntity
import com.salahabusaif.financemanager.core.database.entity.PersonEntity
import com.salahabusaif.financemanager.core.database.entity.PersonLedgerAccountEntity
import com.salahabusaif.financemanager.core.database.entity.PersonOperationEntity
import com.salahabusaif.financemanager.core.database.entity.TransactionGroupEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceRow(
    val ledgerAccountId: String,
    val currencyCode: String,
    val balanceMinor: Long,
)

data class FinancialAccountRow(
    val id: String,
    val name: String,
    val accountKind: String,
    val currencyCode: String,
    val institutionName: String?,
    val providerCode: String?,
    val maskedAccountNumber: String?,
    val isArchived: Boolean,
    val balanceMinor: Long,
)

data class LedgerTransactionRow(
    val id: String,
    val operationId: String,
    val transactionType: String,
    val description: String,
    val currencyCode: String,
    val occurredAt: Long,
    val amountMinor: Long,
    val isReversed: Boolean,
)

data class PersonOperationRow(
    val id: String,
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
    val occurredAt: Long,
)

@Dao
interface LedgerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerson(person: PersonEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPersonAliases(aliases: List<PersonAliasEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPersonLedgerAccount(account: PersonLedgerAccountEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPersonOperation(operation: PersonOperationEntity)

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    suspend fun person(id: String): PersonEntity?

    @Query("SELECT * FROM people WHERE isArchived = 0 ORDER BY displayName COLLATE NOCASE")
    fun people(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM person_aliases WHERE personId = :personId ORDER BY alias COLLATE NOCASE")
    suspend fun aliasesForPerson(personId: String): List<PersonAliasEntity>

    @Query("SELECT person_aliases.* FROM person_aliases WHERE normalizedAlias = :normalizedAlias LIMIT 1")
    suspend fun aliasByNormalizedValue(normalizedAlias: String): PersonAliasEntity?

    @Query("SELECT * FROM person_ledger_accounts WHERE personId = :personId AND role = :role AND currencyCode = :currencyCode LIMIT 1")
    suspend fun personLedgerAccount(personId: String, role: String, currencyCode: String): PersonLedgerAccountEntity?

    @Query("SELECT person_operations.id, person_operations.personId, person_operations.transactionId, person_operations.operationType, person_operations.financialAccountId, person_operations.currencyCode, person_operations.amountMinor, person_operations.commissionMinor, person_operations.fundsHeldChargedMinor, person_operations.beneficiaryName, person_operations.dueDate, person_operations.notes, ledger_transactions.occurredAt FROM person_operations INNER JOIN ledger_transactions ON ledger_transactions.id = person_operations.transactionId WHERE person_operations.personId = :personId AND person_operations.currencyCode = :currencyCode AND ledger_transactions.status = 'POSTED' ORDER BY ledger_transactions.occurredAt DESC")
    fun personOperationRows(personId: String, currencyCode: String): Flow<List<PersonOperationRow>>

    @Query("SELECT person_operations.id, person_operations.personId, person_operations.transactionId, person_operations.operationType, person_operations.financialAccountId, person_operations.currencyCode, person_operations.amountMinor, person_operations.commissionMinor, person_operations.fundsHeldChargedMinor, person_operations.beneficiaryName, person_operations.dueDate, person_operations.notes, ledger_transactions.occurredAt FROM person_operations INNER JOIN ledger_transactions ON ledger_transactions.id = person_operations.transactionId WHERE person_operations.personId = :personId AND person_operations.currencyCode = :currencyCode AND ledger_transactions.status = 'POSTED' ORDER BY ledger_transactions.occurredAt ASC")
    suspend fun personOperationRowsNow(personId: String, currencyCode: String): List<PersonOperationRow>

    @Query("UPDATE people SET displayName = :displayName, normalizedName = :normalizedName, nickname = :nickname, phoneNumber = :phoneNumber, photoPath = :photoPath, notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePerson(id: String, displayName: String, normalizedName: String, nickname: String?, phoneNumber: String?, photoPath: String?, notes: String?, updatedAt: Long): Int

    @Query("UPDATE people SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archivePerson(id: String, archived: Boolean, updatedAt: Long): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOwnerProfile(profile: OwnerProfileEntity)

    @Query("SELECT * FROM owner_profile WHERE id = :id LIMIT 1")
    suspend fun ownerProfile(id: String): OwnerProfileEntity?

    @Query("SELECT * FROM owner_profile WHERE id = :id LIMIT 1")
    fun observeOwnerProfile(id: String): Flow<OwnerProfileEntity?>
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccounts(accounts: List<LedgerAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun seedAccounts(accounts: List<LedgerAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFinancialAccount(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: LedgerTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransactionGroup(group: TransactionGroupEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostings(postings: List<LedgerPostingEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditEvent(event: AuditEventEntity)

    @Query("SELECT * FROM ledger_accounts WHERE id = :id LIMIT 1")
    suspend fun account(id: String): LedgerAccountEntity?

    @Query("SELECT ledger_accounts.* FROM ledger_accounts INNER JOIN financial_accounts ON financial_accounts.ledgerAccountId = ledger_accounts.id WHERE financial_accounts.id = :financialAccountId LIMIT 1")
    suspend fun ledgerAccountForFinancial(financialAccountId: String): LedgerAccountEntity?

    @Query("SELECT * FROM ledger_transactions WHERE operationId = :operationId LIMIT 1")
    suspend fun transactionByOperationId(operationId: String): LedgerTransactionEntity?

    @Query("SELECT * FROM ledger_transactions WHERE status = 'POSTED' ORDER BY accountingDate DESC, createdAt DESC")
    fun postedTransactions(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM ledger_transactions WHERE id = :id LIMIT 1")
    suspend fun transaction(id: String): LedgerTransactionEntity?

    @Query("SELECT * FROM ledger_postings WHERE transactionId = :transactionId ORDER BY sequence")
    suspend fun postingsForTransaction(transactionId: String): List<LedgerPostingEntity>

    @Query("UPDATE financial_accounts SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFinancialAccountArchived(id: String, archived: Boolean, updatedAt: Long): Int

    @Query("UPDATE financial_accounts SET institutionName = :institutionName, maskedAccountNumber = :maskedAccountNumber, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFinancialAccountDetails(id: String, institutionName: String?, maskedAccountNumber: String?, updatedAt: Long): Int

    @Query("UPDATE ledger_accounts SET name = :name, updatedAt = :updatedAt WHERE id = (SELECT ledgerAccountId FROM financial_accounts WHERE id = :financialAccountId)")
    suspend fun renameFinancialLedgerAccount(financialAccountId: String, name: String, updatedAt: Long): Int

    @Query(
        "SELECT ledgerAccountId, currencyCode, " +
            "SUM(CASE WHEN direction = 'DEBIT' THEN amountMinor ELSE -amountMinor END) AS balanceMinor " +
            "FROM ledger_postings INNER JOIN ledger_transactions ON ledger_transactions.id = ledger_postings.transactionId " +
            "WHERE ledger_transactions.status = 'POSTED' AND ledgerAccountId = :accountId GROUP BY ledgerAccountId, currencyCode",
    )
    fun accountBalance(accountId: String): Flow<AccountBalanceRow?>

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN ledger_postings.direction = 'DEBIT' THEN ledger_postings.amountMinor ELSE -ledger_postings.amountMinor END), 0) FROM ledger_postings INNER JOIN ledger_transactions ON ledger_transactions.id = ledger_postings.transactionId WHERE ledger_transactions.status = 'POSTED' AND ledger_postings.ledgerAccountId = :accountId",
    )
    suspend fun postedBalance(accountId: String): Long

    @Query(
        "SELECT financial_accounts.id, ledger_accounts.name, financial_accounts.accountKind, ledger_accounts.currencyCode, " +
            "financial_accounts.institutionName, financial_accounts.providerCode, financial_accounts.maskedAccountNumber, financial_accounts.isArchived, " +
            "COALESCE(balances.balanceMinor, 0) AS balanceMinor " +
            "FROM financial_accounts INNER JOIN ledger_accounts ON ledger_accounts.id = financial_accounts.ledgerAccountId " +
            "LEFT JOIN (SELECT ledger_postings.ledgerAccountId, SUM(CASE WHEN ledger_postings.direction = 'DEBIT' THEN ledger_postings.amountMinor ELSE -ledger_postings.amountMinor END) AS balanceMinor FROM ledger_postings INNER JOIN ledger_transactions ON ledger_transactions.id = ledger_postings.transactionId WHERE ledger_transactions.status = 'POSTED' GROUP BY ledger_postings.ledgerAccountId) AS balances ON balances.ledgerAccountId = ledger_accounts.id " +
            "GROUP BY financial_accounts.id ORDER BY financial_accounts.isArchived, ledger_accounts.name",
    )
    fun financialAccounts(): Flow<List<FinancialAccountRow>>

    @Query(
        "SELECT ledger_transactions.id, ledger_transactions.operationId, ledger_transactions.transactionType, ledger_transactions.description, ledger_postings.currencyCode, COALESCE(ledger_transactions.occurredAt, ledger_transactions.createdAt) AS occurredAt, MAX(ledger_postings.amountMinor) AS amountMinor, EXISTS(SELECT 1 FROM ledger_transactions reversal WHERE reversal.reversalOfId = ledger_transactions.id) AS isReversed FROM ledger_transactions INNER JOIN ledger_postings ON ledger_postings.transactionId = ledger_transactions.id WHERE ledger_transactions.status = 'POSTED' GROUP BY ledger_transactions.id ORDER BY occurredAt DESC",
    )
    fun transactionSummaries(): Flow<List<LedgerTransactionRow>>

    @Transaction
    suspend fun postAtomically(transaction: LedgerTransactionEntity, postings: List<LedgerPostingEntity>) {
        insertTransaction(transaction)
        insertPostings(postings)
    }
}
