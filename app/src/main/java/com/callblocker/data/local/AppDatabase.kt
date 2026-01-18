package com.callblocker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.callblocker.data.local.dao.BlockedCallDao
import com.callblocker.data.local.dao.BlockedNumberDao
import com.callblocker.data.local.dao.SettingsDao
import com.callblocker.data.local.entity.BlockedCallEntity
import com.callblocker.data.local.entity.BlockedNumberEntity
import com.callblocker.data.local.entity.SettingsEntity

@Database(
    entities = [
        BlockedNumberEntity::class,
        BlockedCallEntity::class,
        SettingsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "call_blocker_db"
    }
}
