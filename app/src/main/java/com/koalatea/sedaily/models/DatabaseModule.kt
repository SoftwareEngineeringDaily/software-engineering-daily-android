package com.koalatea.sedaily.models

import androidx.room.Room
import com.koalatea.sedaily.SEDApp

class DatabaseModule {
    companion object {
        fun getDatabase(): AppDatabase {
            return Room
                .databaseBuilder(SEDApp.appContext!!, AppDatabase::class.java, "sedaily")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}