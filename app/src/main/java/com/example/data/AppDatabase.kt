package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CallDao
import com.example.data.dao.ContactDao
import com.example.data.dao.DailyQuotaDao
import com.example.data.model.CallRecord
import com.example.data.model.ContactItem
import com.example.data.model.DailyQuota

@Database(
    entities = [CallRecord::class, ContactItem::class, DailyQuota::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun contactDao(): ContactDao
    abstract fun dailyQuotaDao(): DailyQuotaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "free_call_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
