package com.koalatea.sedaily.feature.player

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.util.Event

class PlayerViewModel(
        private val episodeDetailsRepository: EpisodeDetailsRepository,
        private val playbackManager: PlaybackManager
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val episodeDetailsResource: LiveData<Resource<EpisodeDetails>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            when (val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)) {
                is Resource.Success<EpisodeDetails> -> {
                    _playMediaLiveData.postValue(Event(Pair(resource.data, playbackManager.playbackSpeed)))

                    emit(resource)
                }
                is Resource.Error -> {
                    emit(resource)
                }
            }
        }
    }

    private val _playMediaLiveData = MutableLiveData<Event<Pair<EpisodeDetails, Float>>>()
    val playMediaLiveData: LiveData<Event<Pair<EpisodeDetails, Float>>>
        get() = _playMediaLiveData

    private val _playbackSpeedLiveData = MutableLiveData<Event<Float>>()
    val playbackSpeedLiveData: LiveData<Event<Float>>
        get() = _playbackSpeedLiveData

    @MainThread
    fun play(episodeId: String) {
        episodeIdLiveData.value = episodeId

        if (_playbackSpeedLiveData.value?.peekContent() != playbackManager.playbackSpeed) {
            _playbackSpeedLiveData.value = Event(playbackManager.playbackSpeed)
        }
    }

    fun changePlaybackSpeed(playbackSpeed: Float) {
        playbackManager.playbackSpeed = playbackSpeed

        _playbackSpeedLiveData.value = Event(playbackSpeed)
    }

}