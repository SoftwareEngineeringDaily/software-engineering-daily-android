package com.koalatea.sedaily.feature.player

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.util.Event

class PlayerViewModel(
        private val episodeDetailsRepository: EpisodeDetailsRepository
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val episodeDetailsResource: LiveData<Resource<EpisodeDetails>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            when (val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)) {
                is Resource.Success<EpisodeDetails> -> {
                    _playMediaLiveData.postValue(Event(resource.data))

                    emit(resource)
                }
                is Resource.Error -> {
                    emit(resource)
                }
            }
        }
    }

    private val _playMediaLiveData = MutableLiveData<Event<EpisodeDetails>>()
    val playMediaLiveData: LiveData<Event<EpisodeDetails>>
        get() = _playMediaLiveData

    @MainThread
    fun play(episodeId: String) {
        episodeIdLiveData.value = episodeId
    }

}