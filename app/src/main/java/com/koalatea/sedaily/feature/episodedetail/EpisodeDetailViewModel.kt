package com.koalatea.sedaily.feature.episodedetail

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.feature.downloader.DownloadRepository
import com.koalatea.sedaily.model.Download
import com.koalatea.sedaily.model.Episode
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch


class EpisodeDetailViewModel internal constructor(
        private val episodeDao: EpisodeDao,
        private val downloadRepository: DownloadRepository
) : ViewModel() {
    private val hasDownload = MutableLiveData<Int>()
    private val canPlay = MutableLiveData<Int>()
    private val postTitle = MutableLiveData<String>()
    private val postId = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()
    private val postMp3 = MutableLiveData<String>()
    private val postContent = MutableLiveData<String>()
    val playRequested = SingleLiveEvent<DownloadDao.DownloadEpisode>()
    private var episode: Episode? = null
    private var downloadFile: String? = null

    // @TODO: Replace with composite disposable
    private lateinit var subscription: Disposable
    private lateinit var subscription2: Disposable

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    fun loadEpisode(episodeId: String) {
        viewModelScope.launch {
            val result = episodeDao.findById(episodeId)

            onRetrievePostListSuccess(result)
        }

//        subscription = Observable.fromCallable { episodeDao.findById(episodeId) }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
////            .doOnSubscribe { onRetrivePostListStart() }
////            .doOnTerminate { onRetrievePostListFinish() }
//                .subscribe(
//                        { result -> onRetrievePostListSuccess(result) },
//                        {
//                            Log.v("keithtest", it.localizedMessage)
////                        onRetrievePostListError()
//                        }
//                )
    }

    fun checkForDownload(episodeId: String) = viewModelScope.launch {
        val download = downloadRepository.getDownloadForId(episodeId)

        download?.let {
            onRetrieveDownloadSuccess(download)
        } ?: run {
            // ignore, download state unknown.
        }
    }

    fun getPostTitle(): MutableLiveData<String> {
        return postTitle
    }

    fun getPostImage(): MutableLiveData<String> {
        return postImage
    }

    fun getPostContent(): MutableLiveData<String> {
        return postContent
    }

    fun getHasDownload(): MutableLiveData<Int> {
        return hasDownload
    }

    fun getCanPlay(): MutableLiveData<Int> {
        return canPlay
    }

    private fun onRetrievePostListSuccess(episode: Episode) {
        this.episode = episode

        postTitle.value = episode.title?.rendered
        postImage.value = episode.featuredImage
        postContent.value = episode.content?.rendered ?: ""
        hasDownload.value = View.GONE
        postMp3.value = episode.mp3
        postId.value = episode._id

        if (postMp3.value == null) {
            canPlay.value = View.GONE
        } else {
            canPlay.value = View.VISIBLE
        }

        checkForDownload(episode._id)
    }

    private fun onRetrieveDownloadSuccess(download: Download) {
        hasDownload.value = View.VISIBLE
        downloadFile = download.filename
    }

    fun removeDownloadForId(episodeId: String) {
        viewModelScope.launch {
            hasDownload.value = View.GONE

            downloadRepository.removeDownloadForId(episodeId)
        }
    }

    fun playRequest() {
        if (postMp3.value == null) return

        // @TODO: Create download Episode
        val downloadEpisode: DownloadDao.DownloadEpisode
        if (downloadFile != null) {
            downloadEpisode = DownloadDao.DownloadEpisode(
                    postId.value!!,
                    downloadFile!!,
                    postTitle.value!!,
                    postImage.value
            )
        } else {
            downloadEpisode = DownloadDao.DownloadEpisode(
                    postId.value!!,
                    postMp3.value!!,
                    postTitle.value!!,
                    postImage.value
            )
        }
        playRequested.value = downloadEpisode
    }
}