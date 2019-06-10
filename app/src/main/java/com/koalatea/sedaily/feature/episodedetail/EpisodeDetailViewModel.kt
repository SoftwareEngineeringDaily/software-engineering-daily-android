package com.koalatea.sedaily.feature.episodedetail

import androidx.lifecycle.*
import com.koalatea.sedaily.database.table.Episode
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch
import java.util.*

class EpisodeDetailViewModel internal constructor(
        private val episodeDetailsRepository: EpisodeDetailsRepository
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val episodeDetailsResource: LiveData<Resource<Episode>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)
            resource.data.downloadedId?.let{ downloadedId ->
                val downloadStatusEvent = Event(episodeDetailsRepository.getDownloadStatus(downloadedId)).apply {
                    getContentIfNotHandled()
                }

                _downloadStatusLiveData.postValue(downloadStatusEvent)
            }

            emit(resource)
        }
    }

    private val _downloadStatusLiveData = MutableLiveData<Event<DownloadStatus>>()
    val downloadStatusLiveData: LiveData<Event<DownloadStatus>>
        get() = _downloadStatusLiveData

    fun fetchEpisodeDetails(episodeId: String) {
        if (episodeIdLiveData.value != episodeId) {
            episodeIdLiveData.value = episodeId
        }
    }

    fun download() {
        viewModelScope.launch {
            _downloadStatusLiveData.postValue(Event(DownloadStatus.Downloading(0f)))

            when (val resource = episodeDetailsResource.value) {
                is Resource.Success<Episode> -> {
                    val episode = resource.data
                    val downloadId = episodeDetailsRepository.downloadEpisode(episode)
                    episode.downloadedId = downloadId

                    downloadId?.let {
                        episodeDetailsRepository.addDownload(episode._id, downloadId)

                        monitorDownload(downloadId)
                    } ?: _downloadStatusLiveData.postValue(Event(DownloadStatus.Error()))
                }
                else -> _downloadStatusLiveData.postValue(Event(DownloadStatus.Error()))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val resource = episodeDetailsResource.value) {
                is Resource.Success<Episode> -> {
                    val episode = resource.data
                    episodeDetailsRepository.deleteDownload(episode)
                    episode.downloadedId = null

                    _downloadStatusLiveData.postValue(Event(DownloadStatus.Initial))
                }
            }
        }
    }

    private fun monitorDownload(downloadId: Long) {
        val timer = Timer()
        timer.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        val downloadStatus = episodeDetailsRepository.getDownloadStatus(downloadId)
                        if (downloadStatus !is DownloadStatus.Downloading) {
                            timer.cancel()
                        }

                        _downloadStatusLiveData.postValue(Event(downloadStatus))
                    }
                },
                500L,
                500L)
    }

}