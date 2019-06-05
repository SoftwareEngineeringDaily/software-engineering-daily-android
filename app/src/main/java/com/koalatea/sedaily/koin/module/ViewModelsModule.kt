package com.koalatea.sedaily.koin.module

import com.koalatea.sedaily.feature.auth.AuthViewModel
import com.koalatea.sedaily.feature.downloadList.DownloadsViewModel
import com.koalatea.sedaily.feature.episodedetail.EpisodeDetailViewModel
import com.koalatea.sedaily.feature.episodes.EpisodesViewModel
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.feature.commentList.CommentsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {

    viewModel { EpisodesViewModel(get(), get()) }

    viewModel { CommentsViewModel(get()) }

    viewModel { DownloadsViewModel(get<AppDatabase>().downloadDao(), get()) }

    viewModel { EpisodeDetailViewModel(get<AppDatabase>().episodeDao(), get()) }

    viewModel { AuthViewModel(get(), get()) }

}