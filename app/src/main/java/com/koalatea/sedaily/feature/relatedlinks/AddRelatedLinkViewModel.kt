package com.koalatea.sedaily.feature.relatedlinks

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.feature.relatedlinks.event.ValidationStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch

class AddRelatedLinkViewModel internal constructor(
        private val episodeDetailsRepository: EpisodeDetailsRepository
) : ViewModel() {

    private val _validationLiveData = MutableLiveData<Event<ValidationStatus>>()
    val validationLiveData: LiveData<Event<ValidationStatus>>
        get() = _validationLiveData

    private val _addRelatedLinkLiveData = MutableLiveData<Event<Resource<Boolean>>>()
    val addRelatedLinkLiveData: LiveData<Event<Resource<Boolean>>>
        get() = _addRelatedLinkLiveData

    @MainThread
    fun addRelatedLink(episodeId: String, title: String, url: String) = viewModelScope.launch {
        if (validate(title, url)) {
            _addRelatedLinkLiveData.postValue(Event(Resource.Loading))

            val resource = episodeDetailsRepository.addRelatedLink(episodeId, title, url)

            _addRelatedLinkLiveData.postValue(Event(resource))
        }
    }

    private fun validate(title: String, url: String): Boolean {
        val isTitleValid = title.isNotBlank()
        val isUrlValid = url.isNotBlank() && android.util.Patterns.WEB_URL.matcher(url).matches()

        _validationLiveData.postValue(Event(ValidationStatus(isTitleValid, isUrlValid)))

        return isTitleValid && isUrlValid
    }

}