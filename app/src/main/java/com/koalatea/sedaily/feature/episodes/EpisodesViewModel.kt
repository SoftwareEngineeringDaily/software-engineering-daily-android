package com.koalatea.sedaily.feature.episodes

import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.repository.EpisodesRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch

class EpisodesViewModel internal constructor(
        private val episodesRepository: EpisodesRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    var doNotCache: Boolean = false

    private val searchQueryLiveData = MutableLiveData<SearchQuery>()
    private val episodesResult: LiveData<Result<Episode>> = Transformations.map(searchQueryLiveData) { searchQuery ->
        episodesRepository.fetchEpisodes(searchQuery)
    }

    val episodesPagedList: LiveData<PagedList<Episode>> = Transformations.switchMap(episodesResult) { it.pagedList }
    // FIXME :: This can be delivered more than once.
    val networkState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.refreshState }

    private val _navigateToLogin = MutableLiveData<Event<String>>()
    val navigateToLogin: LiveData<Event<String>>
        get() = _navigateToLogin

    override fun onCleared() {
        super.onCleared()

        if (doNotCache) {
            searchQueryLiveData.value?.let { searchQuery ->
                viewModelScope.launch { episodesRepository.clearLocalCache(searchQuery) }
            }
        }
    }

    @MainThread
    fun fetchPosts(searchQuery: SearchQuery) {
        if (searchQueryLiveData.value != searchQuery) {
            searchQueryLiveData.value = searchQuery
        }
    }

    @MainThread
    fun refresh() = episodesResult.value?.refresh?.invoke()

    @MainThread
    fun toggleUpvote(episode: Episode) {
        viewModelScope.launch {
            if (sessionRepository.isLoggedIn) {
                episodesRepository.vote(episode._id, episode.upvoted
                        ?: false, Math.max(episode.score ?: 0, 0))
            } else {
                _navigateToLogin.value = Event(episode._id)
            }
        }
    }

    @MainThread
    fun toggleBookmark(episode: Episode) {
        viewModelScope.launch {
            if (sessionRepository.isLoggedIn) {
                episodesRepository.bookmark(episode._id, episode.bookmarked ?: false)
            } else {
                _navigateToLogin.value = Event(episode._id)
            }
        }
    }

}