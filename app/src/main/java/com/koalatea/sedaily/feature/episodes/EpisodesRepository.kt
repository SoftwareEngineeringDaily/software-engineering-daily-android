package com.koalatea.sedaily.feature.episodes

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.feature.episodes.paging.EpisodesDataSourceFactory
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.SEDailyApi

class EpisodesRepository(
        private val api: SEDailyApi,
        private val episodeDao: EpisodeDao) {

    fun fetchPosts(searchQuery: SearchQuery, pageSize: Int = 20): Result<Episode> {
        val sourceFactory = EpisodesDataSourceFactory(searchQuery, pageSize, api, episodeDao)

        val livePagedList = LivePagedListBuilder<String?, Episode>(
                sourceFactory, pageSize
        ).build()

        return Result(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
                refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
                refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.refreshState }
        )
    }

}