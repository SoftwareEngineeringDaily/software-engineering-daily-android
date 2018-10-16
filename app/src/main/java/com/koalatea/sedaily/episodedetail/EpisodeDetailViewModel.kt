package com.koalatea.sedaily.episodedetail

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.SEDApp
import com.koalatea.sedaily.downloads.DownloadRepository
import com.koalatea.sedaily.models.Download
import com.koalatea.sedaily.models.Episode
import com.koalatea.sedaily.models.EpisodeDao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class EpisodeDetailViewModel internal constructor(
    private val episodeDao: EpisodeDao
) : ViewModel() {
    private val hasDownload = MutableLiveData<Int>()
    private val postTitle = MutableLiveData<String>()
    private val postId = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()
    private val postMp3 = MutableLiveData<String>()
    private val postContent = MutableLiveData<String>()

    // @TODO: Replace with composite disposable
    private lateinit var subscription: Disposable
    private lateinit var subscription2: Disposable

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    fun loadEpisode(episodeId: String) {
        subscription = Observable.fromCallable { episodeDao.findById(episodeId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .doOnSubscribe { onRetrivePostListStart() }
//            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                {
                    Log.v("keithtest", it.localizedMessage)
//                        onRetrievePostListError()
                }
            )

        checkForDownload(episodeId)
    }

    fun checkForDownload(episodeId: String) {
        subscription2 = DownloadRepository.getDownloadForId(episodeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> onRetrieveDownloadSuccess(result) },
                    {
                        Log.v("keithtest", it.localizedMessage)
//                        onRetrievePostListError()
                    }
                )
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

    private fun onRetrievePostListSuccess(episode: Episode) {
        postTitle.value = episode.title?.rendered
        postImage.value = episode.featuredImage
        postContent.value = episode.content?.rendered ?: ""
        hasDownload.value = View.GONE
        postMp3.value = episode.mp3
        postId.value = episode._id
    }

    private fun onRetrieveDownloadSuccess(download: Download) {
        hasDownload.value = View.VISIBLE
    }
}