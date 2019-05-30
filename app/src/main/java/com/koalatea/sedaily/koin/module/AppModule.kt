package com.koalatea.sedaily.koin.module

import android.preference.PreferenceManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.feature.auth.UserRepository
import com.koalatea.sedaily.feature.downloader.DownloadRepository
import com.koalatea.sedaily.feature.episodes.EpisodesRemoteDataSource
import com.koalatea.sedaily.feature.episodes.EpisodesRepository
import com.koalatea.sedaily.feature.playbar.PodcastSessionStateManager
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

    single { UserRepository(get()) }

    single { DownloadRepository(get<AppDatabase>().downloadDao()) }

    single { EpisodesRemoteDataSource(get()) }
    single { EpisodesRepository(get(), get<AppDatabase>().episodeDao()) }

    single { PodcastSessionStateManager(get()) }

    single { GsonBuilder().create() }

    single { FirebaseAnalytics.getInstance(androidApplication()) }

}