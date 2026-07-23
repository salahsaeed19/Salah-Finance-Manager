package com.salahabusaif.financemanager.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesRepository
import com.salahabusaif.financemanager.core.data.preferences.AppPreferencesSerializer
import com.salahabusaif.financemanager.core.data.preferences.ProtoAppPreferencesRepository
import com.salahabusaif.financemanager.core.data.proto.StoredAppPreferences
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
}
