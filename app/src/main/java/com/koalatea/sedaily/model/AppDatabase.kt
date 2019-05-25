package com.koalatea.sedaily.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Episode::class, Download::class], version = 7, exportSchema = false)
@TypeConverters(EpisodeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun episodeDao(): EpisodeDao
    abstract fun downloadDao(): DownloadDao
}