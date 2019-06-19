package com.koalatea.sedaily.feature.episodedetail

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.feature.episodedetail.event.BookmarkStatus
import com.koalatea.sedaily.feature.episodedetail.event.UpvoteStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.EpisodeDetailsRepository
import com.koalatea.sedaily.repository.EpisodesRepository
import com.koalatea.sedaily.repository.SessionRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch
import java.util.*

class EpisodeDetailViewModel internal constructor(
        private val episodeDetailsRepository: EpisodeDetailsRepository,
        private val episodesRepository: EpisodesRepository,
        private val sessionRepository: SessionRepository
) : ViewModel() {

    private val episodeIdLiveData = MutableLiveData<String>()
    val episodeDetailsResource: LiveData<Resource<Episode>> = Transformations.switchMap(episodeIdLiveData) { episodeId ->
        liveData {
            emit(Resource.Loading)

            when (val resource = episodeDetailsRepository.fetchEpisodeDetails(episodeId)) {
                is Resource.Success<Episode> -> {
                    val episode = resource.data
                    episode.downloadedId?.let { downloadId ->
                        val downloadStatus = episodeDetailsRepository.getDownloadStatus(downloadId)
                        if (downloadStatus is DownloadStatus.Downloading) {
                            monitorDownload(downloadId)
                        }

                        _downloadStatusLiveData.postValue(Event(downloadStatus, userAction = false))
                    }

                    _upvoteLiveData.postValue(Event(UpvoteStatus(episode.upvoted
                            ?: false, Math.max(episode.score ?: 0, 0)), userAction = false))
                    _bookmarkLiveData.postValue(Event(BookmarkStatus(episode.bookmarked
                            ?: false), userAction = false))

                    // FIXME :: Update update play/pause livedata

                    emit(resource)
                }
                is Resource.Error -> {
                    emit(resource)
                }
            }
        }
    }

    private val _downloadStatusLiveData = MutableLiveData<Event<DownloadStatus>>()
    val downloadStatusLiveData: LiveData<Event<DownloadStatus>>
        get() = _downloadStatusLiveData

    private val _navigateToLogin = MutableLiveData<Event<String>>()
    val navigateToLogin: LiveData<Event<String>>
        get() = _navigateToLogin

    private val _upvoteLiveData = MutableLiveData<Event<UpvoteStatus>>()
    val upvoteLiveData: LiveData<Event<UpvoteStatus>>
        get() = _upvoteLiveData

    private val _bookmarkLiveData = MutableLiveData<Event<BookmarkStatus>>()
    val bookmarkLiveData: LiveData<Event<BookmarkStatus>>
        get() = _bookmarkLiveData

    // FIXME :: Add livedata to update play/pause state

    private val episode: Episode?
        get() = (episodeDetailsResource.value as? Resource.Success<Episode>)?.data

    @MainThread
    fun fetchEpisodeDetails(episodeId: String) {
        if (episodeIdLiveData.value != episodeId) {
            episodeIdLiveData.value = episodeId
        }
    }

    @MainThread
    fun download() {
        viewModelScope.launch {
            _downloadStatusLiveData.postValue(Event(DownloadStatus.Downloading(0f)))

            episode?.let { episode ->
                val downloadId = episodeDetailsRepository.downloadEpisode(episode)
                episode.downloadedId = downloadId

                downloadId?.let {
                    episodeDetailsRepository.addDownload(episode._id, downloadId)

                    monitorDownload(downloadId)
                } ?: _downloadStatusLiveData.postValue(Event(DownloadStatus.Error()))
            } ?: _downloadStatusLiveData.postValue(Event(DownloadStatus.Error()))
        }
    }

    @MainThread
    fun delete() {
        viewModelScope.launch {
            episode?.let { episode ->
                episodeDetailsRepository.deleteDownload(episode)
                episode.downloadedId = null

                _downloadStatusLiveData.postValue(Event(DownloadStatus.Initial))
            } ?: _downloadStatusLiveData.postValue(Event(DownloadStatus.Error()))
        }
    }

    @MainThread
    fun toggleUpvote() {
        viewModelScope.launch {
            episode?.let { episode ->
                if (sessionRepository.isLoggedIn) {
                    val currentUpvoteStatus = _upvoteLiveData.value?.peekContent()
                    val originalState = currentUpvoteStatus?.upvoted ?: false
                    val originalScore = Math.max(currentUpvoteStatus?.score ?: 0, 0)
                    val newScore = if (originalState) originalScore - 1 else originalScore + 1
                    _upvoteLiveData.postValue(Event(UpvoteStatus(!originalState, newScore)))

                    val success = episodesRepository.vote(episode._id, originalState, originalScore)
                    if (!success) {
                        _upvoteLiveData.postValue(Event(UpvoteStatus(originalState, originalScore, failed = true)))
                    }
                } else {
                    _navigateToLogin.value = Event(episode._id)
                }
            }
        }
    }

    @MainThread
    fun toggleBookmark() {
        viewModelScope.launch {
            episode?.let { episode ->
                if (sessionRepository.isLoggedIn) {
                    val currentBookmarkStatus = _bookmarkLiveData.value?.peekContent()
                    val originalState = currentBookmarkStatus?.bookmarked ?: false
                    _bookmarkLiveData.postValue(Event(BookmarkStatus(!originalState), userAction = false))

                    val success = episodesRepository.bookmark(episode._id, originalState)
                    if (!success) {
                        _bookmarkLiveData.postValue(Event(BookmarkStatus(originalState, failed = true), userAction = false))
                    }
                } else {
                    _navigateToLogin.value = Event(episode._id)
                }
            }
        }
    }

    @MainThread
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