package com.koalatea.sedaily

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.koalatea.sedaily.auth.AuthViewModel
import com.koalatea.sedaily.downloadList.DownloadsViewModel
import com.koalatea.sedaily.episodedetail.EpisodeDetailViewModel
import com.koalatea.sedaily.home.HomeFeedViewModel
import com.koalatea.sedaily.models.DatabaseModule


class ViewModelFactory(private val activity: AppCompatActivity): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFeedViewModel(DatabaseModule.getDatabase().episodeDao()) as T
        }

        if (modelClass.isAssignableFrom(DownloadsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DownloadsViewModel(DatabaseModule.getDatabase().downloadDao()) as T
        }

        if (modelClass.isAssignableFrom(EpisodeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EpisodeDetailViewModel(DatabaseModule.getDatabase().episodeDao()) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}