package com.koalatea.sedaily.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.downloads.DownloadRepository
import com.koalatea.sedaily.downloads.Downloader
import com.koalatea.sedaily.models.Episode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EpisodeViewModel: ViewModel() {
    private val postTitle = MutableLiveData<String>()
    private val postBody = MutableLiveData<String>()
    private val postMp3 = MutableLiveData<String>()
    private val postId = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()
    private val progress = MutableLiveData<Int>()
    private val downloadVisible = MutableLiveData<Int>()
    private val progressVisible = MutableLiveData<Int>()
    private val playVisible = MutableLiveData<Int>()
    private var episodeData: Episode? = null

    fun bind(episode: Episode) {
        episodeData = episode
        postTitle.value = episode.title?.rendered
        postBody.value = episode.content?.rendered
        postMp3.value = episode.mp3
        postId.value = episode._id
        postImage.value = episode.featuredImage
        progress.value = 0
        downloadVisible.value = View.VISIBLE
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
        if (postMp3.value == null) downloadVisible.value =  View.GONE
        // @TODO: Correct way to check for null?
        // @TOOD: handle disposable
        postId.value?.let {
            DownloadRepository
                .getDownloadForId(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ it ->
                    if (it != null) downloadVisible.value =  View.GONE
                    playVisible.value = View.VISIBLE
                }, {
                    _ ->
                    // @TODO: Log distrbute
                })
        }

        return downloadVisible
    }

    fun getProgressVisible() : MutableLiveData<Int> {
        progressVisible.value =  View.GONE

        return progressVisible
    }

    fun getPlayVisible() : MutableLiveData<Int> {
        playVisible.value =  View.GONE

        return playVisible
    }

    fun download() {
        // @TODO: Check if downloaded from download repo
        postMp3?.apply {
            val progressWatcher = Downloader.downloadMp3(postMp3.value!!, postId.value!!)
            downloadVisible.value =  View.GONE
            progressVisible.value =  View.VISIBLE
            // @TOOD: Get rid of disposables correctly
            progressWatcher?.subscribe {
                progress.value = it!!

                if (it == 100) {
                    progressVisible.value =  View.VISIBLE
                    playVisible.value = View.VISIBLE
                }
            }
        }
        // @TODO log null
    }
}