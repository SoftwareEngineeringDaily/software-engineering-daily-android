package com.koalatea.sedaily.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.R
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.models.DownloadDao
import com.koalatea.sedaily.models.Episode
import com.koalatea.sedaily.models.EpisodeDao
import com.koalatea.sedaily.network.NetworkHelper
import com.koalatea.sedaily.network.SEDailyApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HomeFeedViewModel internal constructor(
    private val episodeDao: EpisodeDao
) : ViewModel() {
    var sedailyApi: SEDailyApi = NetworkHelper.getApi()

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val playRequested = SingleLiveEvent<DownloadDao.DownloadEpisode>()
    val episodes: MutableLiveData<List<Episode>> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    init {
        loadHomeFeed()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun loadHomeFeed() {
        val map = mutableMapOf<String, String>()

        val subscription = sedailyApi.getPosts(map)
                .concatMap {
                    apiPostList -> episodeDao.inserAll(*apiPostList.toTypedArray())
                    Observable.just(apiPostList)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrivePostListStart() }
                .doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                        { result -> onRetrievePostListSuccess(result) },
                        {
                            loadHomeFeedFromLocal()
                        }
                )

        compositeDisposable.add(subscription)
    }

    private fun loadHomeFeedFromLocal() {
        val subscription  = Observable.fromCallable { episodeDao.all }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrivePostListStart() }
                .doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                    { result -> onRetrievePostListSuccess(result) },
                    {
                        onRetrievePostListError()
                    }
                )
        compositeDisposable.add(subscription)
    }

    fun loadHomeFeedAfter() {
        if (loadingVisibility.value == View.VISIBLE) return

        loadingVisibility.value = View.VISIBLE

        val map = mutableMapOf<String, String>()

        val lastIndex = episodes.value?.size?.minus(1) ?: return

        val episodeSize = episodes.value?.size ?: -1
        if (lastIndex > episodeSize) return

        val lastEpisode = lastIndex.let { episodes.value?.get(it) }
        map["createdAtBefore"] = lastEpisode?.date as String

        val subscription = sedailyApi.getPosts(map)
                .concatMap {
                    apiPostList -> episodeDao.inserAll(*apiPostList.toTypedArray())
                    Observable.just(apiPostList)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrivePostListStart() }
                .doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                    {
                        result -> onRetrievePostPageSuccess(result)
                    },
                    {
                        onRetrievePostListError()
                    }
                )

        compositeDisposable.add(subscription)
    }

    fun performSearch(query: String) {
        if (loadingVisibility.value == View.VISIBLE) return

        loadingVisibility.value = View.VISIBLE

        val map = mutableMapOf<String, String>()
        map.set("search", query)

        val subscription = sedailyApi.getPosts(map)
            .concatMap {
                apiPostList -> episodeDao.inserAll(*apiPostList.toTypedArray())
                Observable.just(apiPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrivePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                {
                    result -> onRetrievePostSearchSuccess(result)
                },
                {
                    onRetrievePostListError()
                }
            )

        compositeDisposable.add(subscription)
    }

    private fun onRetrivePostListStart() {
        errorMessage.value = null
    }

    private fun onRetrievePostListFinish() {
        loadingVisibility.value = View.GONE
    }

    private fun onRetrievePostListSuccess(feedList: List<Episode>) {
        episodes.postValue(feedList)
    }

    private fun onRetrievePostPageSuccess(feedList: List<Episode>) {
        episodes.postValue(episodes.value?.plus(feedList))
    }

    private fun onRetrievePostSearchSuccess(feedList: List<Episode>) {
        episodes.postValue(feedList)
    }

    private fun onRetrievePostListError() {
        errorMessage.value = R.string.post_error
    }

    fun play(episode: DownloadDao.DownloadEpisode) {
        playRequested.value = episode
    }
}