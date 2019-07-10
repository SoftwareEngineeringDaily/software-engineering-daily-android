package com.koalatea.sedaily.feature.bookmarks

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodesRepository
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch
import kotlin.math.max

class BookmarksViewModel internal constructor(
        private val episodesRepository: EpisodesRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val bookmarksLiveData = MutableLiveData<Unit>()
    val bookmarksResource: LiveData<Resource<List<Episode>>> = Transformations.switchMap(bookmarksLiveData) {
        liveData {
            emit(Resource.Loading)

            emit(episodesRepository.fetchBookmarks())
        }
    }

    private val _navigateToLogin = MutableLiveData<Event<String>>()
    val navigateToLogin: LiveData<Event<String>>
        get() = _navigateToLogin

    @MainThread
    fun fetchBookmarks() {
        bookmarksLiveData.value = Unit
    }

    @MainThread
    fun toggleUpvote(episode: Episode) {
        viewModelScope.launch {
            if (sessionRepository.isLoggedIn) {
                val success = episodesRepository.vote(episode._id, episode.upvoted
                        ?: false, max(episode.score ?: 0, 0))
                if (success) {
                    bookmarksLiveData.value = Unit
                }
            } else {
                _navigateToLogin.value = Event(episode._id)
            }
        }
    }

    @MainThread
    fun toggleBookmark(episode: Episode) {
        viewModelScope.launch {
            if (sessionRepository.isLoggedIn) {
                val success = episodesRepository.bookmark(episode._id, episode.bookmarked ?: false)
                if (success) {
                    bookmarksLiveData.value = Unit
                }
            } else {
                _navigateToLogin.value = Event(episode._id)
            }
        }
    }

}