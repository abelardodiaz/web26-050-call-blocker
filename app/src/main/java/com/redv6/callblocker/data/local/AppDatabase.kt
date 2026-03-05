package com.redv6.callblocker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.redv6.callblocker.data.local.dao.BlockedCallDao
import com.redv6.callblocker.data.local.dao.BlockedNumberDao
import com.redv6.callblocker.data.local.dao.SettingsDao
import com.redv6.callblocker.data.local.entity.BlockedCallEntity
import com.redv6.callblocker.data.local.entity.BlockedNumberEntity
import com.redv6.callblocker.data.local.entity.SettingsEntity

@Database(
    entities = [
        BlockedNumberEntity::class,
        BlockedCallEntity::class,
        SettingsEntity::class
    ],
    version = 7,
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
