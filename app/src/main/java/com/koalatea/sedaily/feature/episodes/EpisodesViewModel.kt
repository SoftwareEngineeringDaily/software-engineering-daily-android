package com.koalatea.sedaily.feature.episodes

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.network.Result

class EpisodesViewModel internal constructor(
        private val repository: EpisodesRepository
) : ViewModel() {

    private val searchQueryLiveData = MutableLiveData<SearchQuery>()
    private val episodesResult: LiveData<Result<Episode>> = Transformations.map(searchQueryLiveData) { repository.fetchPosts(it) }

    val episodesPagedList: LiveData<PagedList<Episode>> = Transformations.switchMap(episodesResult) { it.pagedList }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.refreshState }


    fun fetchPosts(searchQuery: SearchQuery) = searchQueryLiveData.postValue(searchQuery)

    fun refresh() = episodesResult.value?.refresh?.invoke()

    @Deprecated("")
    fun play(episode: DownloadDao.DownloadEpisode) {
//        playRequested.value = episode
    }

}