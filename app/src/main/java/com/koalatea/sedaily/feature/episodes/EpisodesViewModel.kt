package com.koalatea.sedaily.feature.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.feature.episodes.paging.EpisodesDataSourceFactory
import com.koalatea.sedaily.model.SearchQuery
import java.util.*

class EpisodesViewModel internal constructor(
        private val repository: EpisodesRepository
) : ViewModel() {

    private var createdAtBefore: Date? = null

    private val queryLiveData = MutableLiveData<SearchQuery>()
//    private val episodes: LiveData<Result<List<Episode>>> = Transformations.map(queryLiveData) {
//        GlobalScope.launch(Dispatchers.Main) {
//            return@map repository.fetchPosts(it)
//        }
//    }

//    val episodes: MutableLiveData<List<Episode>> = MutableLiveData()

    val episodesPagedList: LiveData<PagedList<Episode>> by lazy {
        LivePagedListBuilder<String?, Episode>(
                // FIXME ::
                EpisodesDataSourceFactory(queryLiveData.value ?: SearchQuery(), repository), 10
        ).build()
    }

    // FIXME :: Should be a property in the ViewModel
    fun fetchPosts(searchQuery: SearchQuery) {
        queryLiveData.postValue(searchQuery)

//        GlobalScope.launch(Dispatchers.Main) {
//            // TODO :: Add category id and createdAtBefore
//            when (val result = episodesRepository.fetchPosts(categoryId = categoryId)) {
//                is Result.Success -> episodes.setValue(result.data)
//                is Result.ErrorWithCache -> episodes.setValue(result.cachedData)
//                is Result.Error -> {}// Fire an event.
//            }
//        }
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