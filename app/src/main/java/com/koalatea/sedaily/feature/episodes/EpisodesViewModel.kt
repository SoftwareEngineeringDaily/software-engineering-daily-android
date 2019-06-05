package com.koalatea.sedaily.feature.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.feature.auth.UserRepository
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.util.Event

class EpisodesViewModel internal constructor(
        private val episodesRepository: EpisodesRepository,
        private val userRepository: UserRepository
) : ViewModel() {

    private val searchQueryLiveData = MutableLiveData<SearchQuery>()
    private val episodesResult: LiveData<Result<Episode>> = Transformations.map(searchQueryLiveData) { searchQuery ->
        episodesRepository.fetchPosts(searchQuery)
    }

    val episodesPagedList: LiveData<PagedList<Episode>> = Transformations.switchMap(episodesResult) { it.pagedList }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(episodesResult) { it.refreshState }

    private val _navigateToLogin = MutableLiveData<Event<String>>()
    val navigateToLogin: LiveData<Event<String>>
        get() = _navigateToLogin

    fun fetchPosts(searchQuery: SearchQuery) = searchQueryLiveData.postValue(searchQuery)

    fun refresh() = episodesResult.value?.refresh?.invoke()

    fun toggleUpvote(episode: Episode) {
        if (userRepository.isLoggedIn) {
            episodesRepository.vote(episode._id, episode.upvoted ?: false, episode.score ?: 0)
        } else {
            _navigateToLogin.value = Event(episode._id)
        }
    }

    fun toggleBookmark(episode: Episode) {
        if (userRepository.isLoggedIn) {
            episodesRepository.bookmark(episode._id, episode.bookmarked ?: false)
        } else {
            _navigateToLogin.value = Event(episode._id)
        }
    }

    @Deprecated("")
    fun play(episode: DownloadDao.DownloadEpisode) {
//        playRequested.value = episode
    }

}