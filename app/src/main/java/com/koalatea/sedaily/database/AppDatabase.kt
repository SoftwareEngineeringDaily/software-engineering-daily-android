package com.koalatea.sedaily.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.koalatea.sedaily.database.converter.EpisodeConverter
import com.koalatea.sedaily.database.model.Download
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.Listened

@Database(entities = [Episode::class, Download::class, Listened::class], version = 9, exportSchema = false)
@TypeConverters(EpisodeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun episodeDao(): EpisodeDao
    abstract fun downloadDao(): DownloadDao
    abstract fun listenedDao(): ListenedDao

}