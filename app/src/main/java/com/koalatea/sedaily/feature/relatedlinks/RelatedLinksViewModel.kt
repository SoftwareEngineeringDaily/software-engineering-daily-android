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

    private val _addRelatedLinkLiveData = MutableLiveData<Event<Resource<Boolean>>>()
    val addRelatedLinkLiveData: LiveData<Event<Resource<Boolean>>>
        get() = _addRelatedLinkLiveData

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>>
        get() = _navigateToLogin

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
    fun addRelatedLink(title: String, url: String) = viewModelScope.launch {
        if (sessionRepository.isLoggedIn) {
//            _addRelatedLinkLiveData.postValue(Event(Resource.Loading))

//            val resource = episodeDetailsRepository.addRelatedLink(episodeIdLiveData.value, title, url)

//            _addRelatedLinkLiveData.postValue(Event(resource))
        } else {
            _navigateToLogin.value = Event(Unit)
        }
    }

}