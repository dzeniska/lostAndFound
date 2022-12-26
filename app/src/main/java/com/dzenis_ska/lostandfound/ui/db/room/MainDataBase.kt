package com.dzenis_ska.lostandfound.ui.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dzenis_ska.lostandfound.ui.db.room.chat.ChatDao
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.Converters
import com.dzenis_ska.lostandfound.ui.db.room.marker.MainDao
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.MarkerEntity
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.RequestFromBureauEntity


@Database(entities = [MarkerEntity::class, RequestFromBureauEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class MainDataBase: RoomDatabase() {

    abstract fun getDao() : MainDao
    abstract fun getChatDao() : ChatDao

    companion object{
        @Volatile
        private var INSTANCE: MainDataBase? = null
        fun getDataBase(context: Context): MainDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDataBase::class.java,
                    "lost_and_found.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                instance
            }
        }
    }
}