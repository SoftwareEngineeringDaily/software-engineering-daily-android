package com.koalatea.sedaily.feature.episodes.paging

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.util.Event
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodesBoundaryCallback(
        private val searchQuery: SearchQuery,
        private val api: SEDailyApi,
        private val networkManager: NetworkManager,
        private val insertResultIntoDb: (SearchQuery, List<Episode>?) -> Unit,
        private val handleSuccessfulRefresh: (SearchQuery, List<Episode>?) -> Unit,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<EpisodeDetails>() {

    val networkState = MutableLiveData<Event<NetworkState>>()

    private var isRequestInProgress = false

    @MainThread
    override fun onZeroItemsLoaded() {
        load(callback = insertResultIntoDb)
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: EpisodeDetails) {
        // ignored, since we only ever append to what's in the DB
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: EpisodeDetails) {
        load(itemAtEnd.episode.date, insertResultIntoDb)
    }

    @MainThread
    fun refresh() {
        load(callback = handleSuccessfulRefresh)
    }

    @MainThread
    private fun load(createdAtBefore: String? = null, callback: (SearchQuery, List<Episode>?) -> Unit) {
        if (isRequestInProgress) return

        GlobalScope.launch(Dispatchers.Main) {
            isRequestInProgress = true
            networkState.value = Event(NetworkState.Loading)

            val response = safeApiCall {
                api.getEpisodesAsync(
                        searchQuery.searchTerm,
                        searchQuery.categoryId,
                        searchQuery.tagId,
                        createdAtBefore,
                        networkPageSize).await()
            }

            if (response?.isSuccessful == true) {
                val episodes = response.body()?.filter { it.mp3 != null }
                callback(searchQuery, episodes)

                networkState.value = Event(NetworkState.Loaded(episodes?.size ?: 0))
            } else {
                val error = NetworkState.Error(response?.errorBody()?.string(), networkManager.isConnected)

                networkState.value = Event(error)
            }

            isRequestInProgress = false
        }
    }

}