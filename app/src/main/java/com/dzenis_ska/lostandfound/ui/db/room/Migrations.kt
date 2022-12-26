package com.dzenis_ska.lostandfound.ui.db.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE markers ADD COLUMN time_of_creation TEXT NOT NULL DEFAULT '0'")
    }
}