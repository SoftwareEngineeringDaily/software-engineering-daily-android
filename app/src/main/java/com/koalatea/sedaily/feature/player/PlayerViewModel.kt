package com.koalatea.sedaily.feature.player

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.util.Event

class PlayerViewModel(
        private val episodeDetailsRepository: EpisodeDetailsRepository
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val episodeDetailsResource: LiveData<Resource<Episode>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            when (val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)) {
                is Resource.Success<Episode> -> {
                    _playMediaLiveData.postValue(Event(resource.data))

                    emit(resource)
                }
                is Resource.Error -> {
                    emit(resource)
                }
            }
        }
    }

    private val _playMediaLiveData = MutableLiveData<Event<Episode>>()
    val playMediaLiveData: LiveData<Event<Episode>>
        get() = _playMediaLiveData

    private val episode: Episode?
        get() = (episodeDetailsResource.value as? Resource.Success<Episode>)?.data

    @MainThread
    fun play(episodeId: String) {
//        if (episodeIdLiveData.value != episodeId) {
            episodeIdLiveData.value = episodeId
//        }
    }

}