package com.koalatea.sedaily.feature.episodes.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.SEDailyApi

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI.
 */
class EpisodesDataSourceFactory(
        private val searchQuery: SearchQuery,
        private val pageSize: Int,
        private val api: SEDailyApi,
        private val episodeDao: EpisodeDao)
    : DataSource.Factory<String?, Episode>() {

    val sourceLiveData = MutableLiveData<EpisodesPagingDataSource>()

    override fun create(): DataSource<String?, Episode> {
        val source = EpisodesPagingDataSource(searchQuery, pageSize, api, episodeDao)
        sourceLiveData.postValue(source)

        return source
    }

}