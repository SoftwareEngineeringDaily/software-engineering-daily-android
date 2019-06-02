package com.koalatea.sedaily.feature.episodes.paging

import androidx.paging.ItemKeyedDataSource
import com.koalatea.sedaily.feature.episodes.EpisodesRepository
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodesPagingDataSource(private val searchQuery: SearchQuery, private val repository: EpisodesRepository) : ItemKeyedDataSource<String?, Episode>() {

    override fun loadInitial(params: LoadInitialParams<String?>, callback: LoadInitialCallback<Episode>) = load(params.requestedInitialKey, callback)

    override fun loadAfter(params: LoadParams<String?>, callback: LoadCallback<Episode>) = load(params.key, callback)

    override fun loadBefore(params: LoadParams<String?>, callback: LoadCallback<Episode>) {
        // Ignored, since we only ever append to our initial load.
    }

    override fun getKey(item: Episode) = item.date ?: ""

    private fun load(key: String?, callback: LoadCallback<Episode>) {
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = repository.fetchPosts(searchQuery, key)) {
                is Result.Success -> callback.onResult(result.data)
                is Result.ErrorWithCache -> callback.onResult(result.cachedData)
                is Result.Error -> {}//onFailure(call, new HttpException (response))
            }
        }
    }

}