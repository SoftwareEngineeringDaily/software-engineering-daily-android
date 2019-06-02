package com.koalatea.sedaily.feature.episodes.paging

import androidx.paging.DataSource
import com.koalatea.sedaily.feature.episodes.EpisodesRepository
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery

class EpisodesDataSourceFactory(
        private val searchQuery: SearchQuery,
        private val repository: EpisodesRepository)
    : DataSource.Factory<String?, Episode>() {

    override fun create(): DataSource<String?, Episode> = EpisodesPagingDataSource(searchQuery, repository)

}