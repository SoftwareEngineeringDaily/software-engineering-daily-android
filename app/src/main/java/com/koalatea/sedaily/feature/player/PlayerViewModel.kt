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

    private val episodeIdLiveData = MutableLiveData<Pair<String, Boolean>>()
    val episodeDetailsResource: LiveData<Resource<EpisodeDetails>> = Transformations.switchMap(episodeIdLiveData) { (episodeId, forcePlay) ->
        liveData {
            emit(Resource.Loading)

            when (val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)) {
                is Resource.Success<EpisodeDetails> -> {
                    if (forcePlay) {
                        _playMediaLiveData.postValue(Event(resource.data))
                    }

                    if (_playbackSpeedLiveData.value?.peekContent() != playbackManager.playbackSpeed) {
                        _playbackSpeedLiveData.value = Event(playbackManager.playbackSpeed)
                    }

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

    private val _playbackSpeedLiveData = MutableLiveData<Event<Float>>()
    val playbackSpeedLiveData: LiveData<Event<Float>>
        get() = _playbackSpeedLiveData

    @MainThread
    fun refreshIfNecessary(episodeId: String) {
        if (episodeDetailsResource.value == null) {
            episodeIdLiveData.value = Pair(episodeId, false)
        }
    }

    @MainThread
    fun play(episodeId: String) {
        episodeIdLiveData.value = Pair(episodeId, true)
    }

    @MainThread
    fun changePlaybackSpeed(playbackSpeed: Float) {
        playbackManager.playbackSpeed = playbackSpeed

        _playbackSpeedLiveData.value = Event(playbackSpeed)
    }

}