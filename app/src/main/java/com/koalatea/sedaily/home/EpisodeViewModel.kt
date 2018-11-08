package com.koalatea.sedaily.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.SEDApp
import com.koalatea.sedaily.downloadManager.DownloadEpisodeEvent
import com.koalatea.sedaily.downloadManager.DownloadRepository
import com.koalatea.sedaily.downloadManager.Downloader
import com.koalatea.sedaily.downloadManager.DownloaderServiceManager
import com.koalatea.sedaily.models.DownloadDao
import com.koalatea.sedaily.models.Episode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class EpisodeViewModel(private val homeFeedViewModel: HomeFeedViewModel): ViewModel() {
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
        postBody.value = episode.content?.rendered
        postMp3.value = episode.mp3
        postId.value = episode._id
        postImage.value = episode.featuredImage
        progress.value = 0
        downloadVisible.value = View.VISIBLE
        streamVisible.value = View.VISIBLE
    }

    fun getPostTitle(): MutableLiveData<String> {
        return postTitle
    }

    fun getProgress(): MutableLiveData<Int>{
        return progress
    }

    fun getPostMp3(): MutableLiveData<String>  {
        return postMp3
    }

    fun getPostImage(): MutableLiveData<String> {
        return postImage
    }

    @SuppressLint("CheckResult")
    fun getDownloadVisible() : MutableLiveData<Int> {
        if (postMp3.value == null) {
            downloadVisible.value = View.GONE
            streamVisible.value = View.GONE
        }

        postId.value?.let {
            val subscriber = DownloadRepository
                .getDownloadForId(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ download ->
                    if (download != null) downloadVisible.value =  View.GONE
                    playVisible.value = View.VISIBLE
                    streamVisible.value = View.GONE
                    downloadFile = download.filename
                }, {
                    _ ->
                    // @TODO: Log distribute
                })

            compositeDisposable.add(subscriber)
        }

        return downloadVisible
    }

    fun getProgressVisible() : MutableLiveData<Int> {
        progressVisible.value =  View.GONE

        if (Downloader.downloadingFiles.contains(episodeData?._id)) {
            subscribeToDownload()
        }

        return progressVisible
    }

    fun getPlayVisible() : MutableLiveData<Int> {
        playVisible.value =  View.GONE

        return playVisible
    }

    fun getStreamVisible(): MutableLiveData<Int> {
        return streamVisible
    }

    fun download() {
        // @TODO: Check if downloaded from download repo
        postMp3.apply {
            DownloaderServiceManager.startBackgroundDownload(SEDApp.appContext!!, postId.value, postMp3.value)
            subscribeToDownload()
        }
        // @TODO log null
    }

    private fun subscribeToDownload() {
        downloadVisible.value = View.GONE
        streamVisible.value = View.GONE
        progressVisible.value =  View.VISIBLE

        val subscriber = Downloader
                .currentDownloadProgress
                .subscribe (this@EpisodeViewModel::handleDownloadEvent) {
                    Log.v("sedaily-debug", it.localizedMessage)
                }
        compositeDisposable.add(subscriber)
    }

    private fun handleDownloadEvent(downloadEvent: DownloadEpisodeEvent) {
        if (downloadEvent.episodeId == postId.value && downloadEvent.progress != progress.value) {
            progress.value = downloadEvent.progress!!
            if (downloadEvent.progress == 100) {
                progressVisible.value =  View.GONE
                playVisible.value = View.VISIBLE
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