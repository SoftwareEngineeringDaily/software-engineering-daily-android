package com.koalatea.sedaily.feature.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.SEDApplication
import com.koalatea.sedaily.feature.downloader.DownloadEpisodeEvent
import com.koalatea.sedaily.feature.downloader.DownloadRepository
import com.koalatea.sedaily.model.DownloadDao
import com.koalatea.sedaily.model.Episode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodeViewHolderViewModel(
        private val homeFeedViewModel: HomeFeedViewModel,
        private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val postTitle = MutableLiveData<String>()
    private val postBody = MutableLiveData<String>()
    private val postMp3 = MutableLiveData<String>()
    private val postId = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()
    private val progress = MutableLiveData<Int>()
    private val downloadVisible = MutableLiveData<Int>()
    private val progressVisible = MutableLiveData<Int>()
    private val playVisible = MutableLiveData<Int>()
    private val streamVisible = MutableLiveData<Int>()
    private var episodeData: Episode? = null
    private var downloadFile: String? = null

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun bind(episode: Episode) {
        episodeData = episode
        postTitle.value = episode.title?.rendered
        postBody.value = getPostBodyString(episode)
        postMp3.value = episode.mp3
        postId.value = episode._id
        postImage.value = episode.featuredImage
        progress.value = 0
        downloadVisible.value = View.VISIBLE
        streamVisible.value = View.VISIBLE
    }

    @Deprecated("Move formatting to the xml")
    fun getPostBodyString(episode: Episode): String {
        if (episode.excerpt?.rendered == null) return ""

        var end = 100
        if (episode.excerpt.rendered.length < 100) {
            end = episode.excerpt.rendered.length
        }

        return HtmlCompat.fromHtml(episode.excerpt.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString()//.subSequence(0, end).toString() + "..."
    }

    fun getPostTitle(): MutableLiveData<String> {
        return postTitle
    }

    fun getPostBody(): MutableLiveData<String> {
        return postBody
    }

    fun getProgress(): MutableLiveData<Int> {
        return progress
    }

    fun getPostMp3(): MutableLiveData<String> {
        return postMp3
    }

    fun getPostImage(): MutableLiveData<String> {
        return postImage
    }

    @SuppressLint("CheckResult")
    fun getDownloadVisible(): MutableLiveData<Int> {
        if (postMp3.value == null) {
            downloadVisible.value = View.GONE
            streamVisible.value = View.GONE
        }

        postId.value?.let {
            GlobalScope.launch(Dispatchers.Main) {
                val download = downloadRepository.getDownloadForId(it)

                download?.let {
                    playVisible.value = View.VISIBLE
                    streamVisible.value = View.GONE
                    downloadFile = download.filename
                } ?: run {
                    downloadVisible.value = View.GONE
                }
            }
        }

        return downloadVisible
    }

    fun getProgressVisible(): MutableLiveData<Int> {
        progressVisible.value = View.GONE

        // FIXME :: Check if we are currently downloading this episode.
//        if (Downloader.downloadingFiles.contains(episodeData?._id)) {
//            subscribeToDownload()
//        }

        return progressVisible
    }

    fun getPlayVisible(): MutableLiveData<Int> {
        playVisible.value = View.GONE

        return playVisible
    }

    fun getStreamVisible(): MutableLiveData<Int> {
        return streamVisible
    }

    fun download() {
        // FIXME :: Start download and subscribe to it
//        postMp3.apply {
//            DownloaderServiceManager.startBackgroundDownload(SEDApplication.appContext!!, postId.value, postMp3.value)
//            subscribeToDownload()
//        }
    }

    private fun subscribeToDownload() {
        downloadVisible.value = View.GONE
        streamVisible.value = View.GONE
        progressVisible.value = View.VISIBLE

        // FIXME :: Check download progress
//        val subscriber = Downloader
//                .currentDownloadProgress
//                .subscribe(this@EpisodeViewHolderViewModel::handleDownloadEvent) {
//                    Log.v("sedaily-debug", it.localizedMessage)
//                }
//        compositeDisposable.add(subscriber)
    }

    private fun handleDownloadEvent(downloadEvent: DownloadEpisodeEvent) {
        if (downloadEvent.episodeId == postId.value) {
            progress.value = downloadEvent.progress!!
            if (downloadEvent.progress == 100) {
                progressVisible.value = View.GONE
                playVisible.value = View.VISIBLE

                // FIXME :: Specify download directory.
//                downloadFile = Downloader.getDirectoryForEpisodes() + downloadEvent.episodeId + ".mp3"
            }
        }
    }

    fun playRequest() {
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

        homeFeedViewModel.play(downloadEpisode)
    }
}