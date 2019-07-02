package com.koalatea.sedaily.feature.relatedlinks

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.model.RelatedLink
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch

class RelatedLinksViewModel internal constructor(
        private val episodeDetailsRepository: EpisodeDetailsRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val relatedLinksResource: LiveData<Resource<List<RelatedLink>>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            emit(episodeDetailsRepository.fetchRelatedLinks(episodeId))
        }
    }

    val episodeId: String?
        get() = episodeIdLiveData.value

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>>
        get() = _navigateToLogin

    private val _navigateToAddRelatedLink = MutableLiveData<Event<Unit>>()
    val navigateToAddRelatedLink: LiveData<Event<Unit>>
        get() = _navigateToAddRelatedLink

    @MainThread
    fun fetchRelatedLinks(episodeId: String) {
        if (episodeIdLiveData.value != episodeId) {
            episodeIdLiveData.value = episodeId
        }
    }

    @MainThread
    fun reloadRelatedLinks() {
        episodeIdLiveData.value = episodeIdLiveData.value
    }

    @MainThread
    fun addRelatedLink() = viewModelScope.launch {
        if (sessionRepository.isLoggedIn) {
            _navigateToAddRelatedLink.value = Event(Unit)
        } else {
            _navigateToLogin.value = Event(Unit)
        }
    }

}