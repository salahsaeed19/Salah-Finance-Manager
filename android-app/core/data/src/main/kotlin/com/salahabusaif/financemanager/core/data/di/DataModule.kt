package com.salahabusaif.financemanager.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesRepository
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesSerializer
import com.salahabusaif.financemanager.core.data.preferences.ProtoAppPreferencesRepository
import com.salahabusaif.financemanager.core.data.ledger.RoomLedgerGateway
import com.salahabusaif.financemanager.core.data.profile.OwnerProfileRepository
import com.salahabusaif.financemanager.core.data.profile.RoomOwnerProfileRepository
import com.salahabusaif.financemanager.core.data.proto.StoredAppPreferences
import com.salahabusaif.financemanager.core.database.FinanceDatabase
import com.salahabusaif.financemanager.core.database.FinanceDatabaseMigrations
import com.salahabusaif.financemanager.core.ledger.LedgerGateway
import com.salahabusaif.financemanager.core.ledger.PeopleGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindings {
    @Binds abstract fun bindPreferencesRepository(implementation: ProtoAppPreferencesRepository): AppPreferencesRepository

    @Binds abstract fun bindLedgerGateway(implementation: RoomLedgerGateway): LedgerGateway

    @Binds abstract fun bindPeopleGateway(implementation: RoomLedgerGateway): PeopleGateway

    @Binds abstract fun bindOwnerProfileRepository(implementation: RoomOwnerProfileRepository): OwnerProfileRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<StoredAppPreferences> =
        DataStoreFactory.create(
            serializer = AppPreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler { AppPreferencesSerializer.defaultValue },
            produceFile = { File(context.filesDir, "datastore/app_preferences.pb") },
        )

    @Provides
    @Singleton
    fun provideFinanceDatabase(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, FinanceDatabase.NAME)
            .addMigrations(FinanceDatabaseMigrations.MIGRATION_1_2)
            .addMigrations(FinanceDatabaseMigrations.MIGRATION_2_3)
            .addMigrations(FinanceDatabaseMigrations.MIGRATION_3_4)
            .addMigrations(FinanceDatabaseMigrations.MIGRATION_4_5)
            .build()
}
