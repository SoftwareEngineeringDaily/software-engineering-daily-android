package com.koalatea.sedaily.feature.episodes

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.R
import com.koalatea.sedaily.SingleLiveEvent
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.SEDailyApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EpisodesViewModel internal constructor(
        private val episodesRepository: EpisodesRepository
) : ViewModel() {

    private var createdAtBefore: Date? = null

    val episodes: MutableLiveData<List<Episode>> = MutableLiveData()

    // FIXME :: Should be a property in the ViewModel
    fun fetchPosts(categoryId: String? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            // TODO :: Add category id and createdAtBefore
            when (val result = episodesRepository.fetchPosts(categoryId = categoryId)) {
                is Result.Success -> episodes.setValue(result.data)
                is Result.ErrorWithCache -> episodes.setValue(result.cachedData)
                is Result.Error -> {}// Fire an event.
            }
        }
    }

    private fun reset() {
        createdAtBefore = null
    }

//    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
//    val errorMessage: MutableLiveData<Int> = MutableLiveData()
//    val playRequested = SingleLiveEvent<DownloadDao.DownloadEpisode>()
//    val episodes: MutableLiveData<List<Episode>> = MutableLiveData()
//
//    private val compositeDisposable = CompositeDisposable()
//
//    override fun onCleared() {
//        super.onCleared()
//        compositeDisposable.dispose()
//    }
//
//    fun loadHomeFeed() {
//        val map = mutableMapOf<String, String>()
//
//
//        val subscription = sedailyApi.getPostsAsync(map)
//                .concatMap { apiPostList ->
//                    episodeDao.insert(*apiPostList.toTypedArray())
//                    Observable.just(apiPostList)
//                }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { onRetrivePostListStart() }
//                .doOnTerminate { onRetrievePostListFinish() }
//                .subscribe(
//                        { result -> onRetrievePostListSuccess(result) },
//                        {
//                            loadHomeFeedFromLocal()
//                        }
//                )
//
//        compositeDisposable.add(subscription)
//    }

//    private fun loadHomeFeedFromLocal() {
//        val subscription = Observable.fromCallable { episodeDao.all }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { onRetrivePostListStart() }
//                .doOnTerminate { onRetrievePostListFinish() }
//                .subscribe(
//                        { result -> onRetrievePostListSuccess(result) },
//                        {
//                            onRetrievePostListError()
//                        }
//                )
//        compositeDisposable.add(subscription)
//    }

//    fun loadHomeFeedAfter() {
//        if (loadingVisibility.value == View.VISIBLE) return
//
//        loadingVisibility.value = View.VISIBLE
//
//        val map = mutableMapOf<String, String>()
//
//        val lastIndex = episodes.value?.size?.minus(1) ?: return
//
//        val episodeSize = episodes.value?.size ?: -1
//        if (lastIndex > episodeSize || episodeSize == -1) return
//
//        val lastEpisode = lastIndex.let { episodes.value?.get(it) }
//        map["createdAtBefore"] = lastEpisode?.date as String
//
//        val subscription = sedailyApi.getPostsAsync(map)
//                .concatMap { apiPostList ->
//                    episodeDao.insert(*apiPostList.toTypedArray())
//                    Observable.just(apiPostList)
//                }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { onRetrivePostListStart() }
//                .doOnTerminate { onRetrievePostListFinish() }
//                .subscribe(
//                        { result ->
//                            onRetrievePostPageSuccess(result)
//                        },
//                        {
//                            onRetrievePostListError()
//                        }
//                )
//
//        compositeDisposable.add(subscription)
//    }

//    fun performSearch(query: String) {
//        if (loadingVisibility.value == View.VISIBLE) return
//
//        loadingVisibility.value = View.VISIBLE
//
//        val map = mutableMapOf<String, String>()
//        map.set("search", query)
//
//        val subscription = sedailyApi.getPostsAsync(map)
//                .concatMap { apiPostList ->
//                    episodeDao.insert(*apiPostList.toTypedArray())
//                    Observable.just(apiPostList)
//                }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { onRetrivePostListStart() }
//                .doOnTerminate { onRetrievePostListFinish() }
//                .subscribe(
//                        { result ->
//                            onRetrievePostSearchSuccess(result)
//                        },
//                        {
//                            onRetrievePostListError()
//                        }
//                )
//
//        compositeDisposable.add(subscription)
//    }
//
//    private fun onRetrivePostListStart() {
//        errorMessage.value = null
//    }
//
//    private fun onRetrievePostListFinish() {
//        loadingVisibility.value = View.GONE
//    }
//
//    private fun onRetrievePostListSuccess(feedList: List<Episode>) {
//        episodes.postValue(feedList)
//    }
//
//    private fun onRetrievePostPageSuccess(feedList: List<Episode>) {
//        episodes.postValue(episodes.value?.plus(feedList))
//    }
//
//    private fun onRetrievePostSearchSuccess(feedList: List<Episode>) {
//        episodes.postValue(feedList)
//    }
//
//    private fun onRetrievePostListError() {
//        errorMessage.value = R.string.post_error
//    }
//
    fun play(episode: DownloadDao.DownloadEpisode) {
//        playRequested.value = episode
    }
}