package com.koalatea.sedaily.feature.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.feature.episodes.paging.EpisodesDataSourceFactory
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery

class EpisodesViewModel internal constructor(
        private val repository: EpisodesRepository
) : ViewModel() {

    private val searchQueryLiveData = MutableLiveData<SearchQuery>()
    val episodesPagedList: LiveData<PagedList<Episode>> = Transformations.switchMap(searchQueryLiveData) {
        LivePagedListBuilder<String?, Episode>(
                EpisodesDataSourceFactory(it, repository), it.pageSize
        ).build()
    }

    fun fetchPosts(searchQuery: SearchQuery) = searchQueryLiveData.postValue(searchQuery)

    @Deprecated("")
    fun play(episode: DownloadDao.DownloadEpisode) {
//        playRequested.value = episode
    }

}