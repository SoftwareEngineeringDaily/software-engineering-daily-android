package com.koalatea.sedaily.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.koalatea.sedaily.database.table.Download
import com.koalatea.sedaily.database.table.Episode
import com.koalatea.sedaily.database.converter.EpisodeConverter

@Database(entities = [Episode::class, Download::class], version = 8, exportSchema = false)
@TypeConverters(EpisodeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun episodeDao(): EpisodeDao
    abstract fun downloadDao(): DownloadDao

}