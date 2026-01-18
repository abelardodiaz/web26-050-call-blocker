package com.callblocker.core.di

import android.content.Context
import androidx.room.Room
import com.callblocker.data.local.AppDatabase
import com.callblocker.data.local.migration.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4, Migrations.MIGRATION_4_5, Migrations.MIGRATION_5_6)
            .build()
    }

    @Provides
    fun provideBlockedNumberDao(database: AppDatabase) = database.blockedNumberDao()

    @Provides
    fun provideBlockedCallDao(database: AppDatabase) = database.blockedCallDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase) = database.settingsDao()
}
