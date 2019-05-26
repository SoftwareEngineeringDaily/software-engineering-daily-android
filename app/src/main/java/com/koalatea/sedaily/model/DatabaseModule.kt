package com.koalatea.sedaily.model

import androidx.room.Room
import com.koalatea.sedaily.SEDApp

class DatabaseModule {
    companion object {
        @Volatile
        private var dbInstance: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return dbInstance ?: synchronized(this) {
                dbInstance ?: buildDatabase().also { dbInstance = it }
            }
        }

        fun buildDatabase(): AppDatabase {
            return Room
                    .databaseBuilder(SEDApp.appContext!!, AppDatabase::class.java, "sedaily")
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}