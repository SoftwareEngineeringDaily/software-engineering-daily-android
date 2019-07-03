package com.koalatea.sedaily.koin.module

import android.preference.PreferenceManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.downloader.DownloadManager
import com.koalatea.sedaily.feature.player.PlaybackManager
import com.koalatea.sedaily.repository.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {

    single { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }

    single {
        Room
                .databaseBuilder(androidApplication(), AppDatabase::class.java, "sedaily")
                .fallbackToDestructiveMigration()
                .build()
    }

    single { DownloadManager(androidApplication()) }

    single { SessionRepository(get()) }
    single { UserRepository(get(), get(), get(), get(), get()) }

    single { EpisodeDetailsRepository(get(), get(), get(), get()) }

    factory { EpisodesRepository(get(), get(), get(), get(), get()) }

    single { CommentsRepository(get(), get(), get()) }

    single { PlaybackManager(get()) }

    single { GsonBuilder().create() }

    single { FirebaseAnalytics.getInstance(androidApplication()) }

}