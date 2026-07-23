package com.salahabusaif.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.salahabusaif.financemanager.core.database.entity.DatabaseMetadataEntity

@Dao
interface DatabaseMetadataDao {
    @Query("SELECT * FROM database_metadata WHERE `key` = :key LIMIT 1")
    suspend fun find(key: String): DatabaseMetadataEntity?
}
