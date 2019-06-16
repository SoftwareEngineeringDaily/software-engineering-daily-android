package com.koalatea.sedaily.koin.module

import android.preference.PreferenceManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.feature.auth.UserRepository
import com.koalatea.sedaily.feature.commentList.CommentsRepository
import com.koalatea.sedaily.feature.downloader.DownloadManager
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.repository.EpisodesRepository
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

    single { UserRepository(get()) }

    single { EpisodeDetailsRepository(get(), get(), get()) }

    factory { EpisodesRepository(get(), get()) }

    single { CommentsRepository(get()) }

    single { GsonBuilder().create() }

    single { FirebaseAnalytics.getInstance(androidApplication()) }

}