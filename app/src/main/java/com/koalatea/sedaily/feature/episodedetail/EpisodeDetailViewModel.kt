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

            emit(episodeDetailsRepository.fetchEpisodeDetails(episodeId))
        }
    }

    private val _downloadProgressLiveData = MutableLiveData<Float>()
    val downloadProgressLiveData: LiveData<Float>
        get() = _downloadProgressLiveData

    private val _downloadDoneLiveData = MutableLiveData<Event<Unit>>()
    val downloadDoneLiveData: LiveData<Event<Unit>>
        get() = _downloadDoneLiveData

    private val _downloadErrorLiveData = MutableLiveData<Event<String?>>()
    val downloadErrorLiveData: LiveData<Event<String?>>
        get() = _downloadErrorLiveData

    private val _deleteDownloadDoneLiveData = MutableLiveData<Event<Unit>>()
    val deleteDownloadDoneLiveData: LiveData<Event<Unit>>
        get() = _deleteDownloadDoneLiveData

    fun fetchEpisodeDetails(episodeId: String) = episodeIdLiveData.postValue(episodeId)

    fun download() {
        viewModelScope.launch {
            _downloadProgressLiveData.postValue(0f)

            when (val resource = episodeDetailsResource.value) {
                is Resource.Success<Episode> -> {
                    val episode = resource.data
                    val downloadId = episodeDetailsRepository.downloadEpisode(episode)
                    downloadId?.let {
                        episodeDetailsRepository.addDownload(episode._id, downloadId)

                        monitorDownload(downloadId)
                    } ?: _downloadErrorLiveData.postValue(Event(null))
                }
                else -> _downloadErrorLiveData.postValue(Event(null))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val resource = episodeDetailsResource.value) {
                is Resource.Success<Episode> -> {
                    val episode = resource.data

                    episodeDetailsRepository.deleteDownload(episode)

                    _deleteDownloadDoneLiveData.value = Event(Unit)
                }
            }
        }
    }

    private fun monitorDownload(downloadId: Long) {
        val timer = Timer()
        timer.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        when (val downloadStatus = episodeDetailsRepository.getDownloadStatus(downloadId)) {
                            is DownloadStatus.Downloading -> _downloadProgressLiveData.postValue(downloadStatus.progress)
                            is DownloadStatus.Downloaded -> {
                                _downloadDoneLiveData.postValue(Event(Unit))
                                timer.cancel()
                            }
                            is DownloadStatus.Error -> {
                                _downloadErrorLiveData.postValue(Event(downloadStatus.reason))
                                timer.cancel()
                            }
                            else -> timer.cancel()
                        }
                    }
                },
                500L,
                500L)
    }

}