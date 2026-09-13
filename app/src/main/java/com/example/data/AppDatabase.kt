package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PropertyDao
import com.example.data.dao.ReviewDao
import com.example.data.dao.TransactionDao
import com.example.data.model.PropertyEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.TransactionEntity

@Database(
    entities = [
        PropertyEntity::class,
        TransactionEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "estateiq_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
