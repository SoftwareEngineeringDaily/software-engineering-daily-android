package com.koalatea.sedaily.downloads

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.R
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.models.DownloadDao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DownloadsViewModel internal constructor(
        private val downloadsDao: DownloadDao
) : ViewModel() {
    val downloadListAdapter: DownloadsListAdapter = DownloadsListAdapter(this)

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val errorClickListener = View.OnClickListener { loadFeed() }
    val playRequested = SingleLiveEvent<DownloadDao.DownloadEpisode>()

    private lateinit var subscription: Disposable

    init {
        loadFeed()
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    private fun loadFeed() {
        subscription = Observable.fromCallable { downloadsDao.allDownloadsWithEpisodes }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                {
                    Log.v("keithtest", it.localizedMessage)
                    onRetrievePostListError()
                }
            )
    }

    private fun onRetrievePostListStart() {
        loadingVisibility.value = View.VISIBLE
        errorMessage.value = null
    }

    private fun onRetrievePostListFinish() {
        loadingVisibility.value = View.GONE
    }

    private fun onRetrievePostListSuccess(feedList: List<DownloadDao.DownloadEpisode>) {
        downloadListAdapter.updateFeedList(feedList)
    }

    private fun onRetrievePostListError() {
        errorMessage.value = R.string.post_error
    }

    fun play(episode: DownloadDao.DownloadEpisode) {
        playRequested.value = episode
    }
}