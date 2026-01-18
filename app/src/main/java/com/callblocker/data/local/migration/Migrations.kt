package com.callblocker.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE blocked_numbers ADD COLUMN is_prefix INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE settings ADD COLUMN enabled_sim_slots TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE settings ADD COLUMN persistent_service_enabled INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE blocked_calls ADD COLUMN simSlot INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE settings ADD COLUMN developer_mode_enabled INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE settings ADD COLUMN dev_sim_detection_by_format INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE settings ADD COLUMN dev_block_sim1 INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE settings ADD COLUMN dev_block_sim2 INTEGER NOT NULL DEFAULT 1")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE settings ADD COLUMN app_language TEXT NOT NULL DEFAULT 'system'")
        }
    }
}
