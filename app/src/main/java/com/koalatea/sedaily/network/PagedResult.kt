package com.koalatea.sedaily.network

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.koalatea.sedaily.util.Event

// Inspired by https://github.com/googlesamples/android-architecture-components/blob/corotuine-network-bound-resource/PagingWithNetworkSample/app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/Listing.kt
data class PagedResult<T>(
        // The LiveData of paged lists for the UI to observe
        val pagedList: LiveData<PagedList<T>>,
        // Represents the network request status to show to the user
        val networkState: LiveData<Event<NetworkState>>,
        // Refreshes the whole data and fetches it from scratch.
        val refresh: () -> Unit
)

sealed class NetworkState {
    object Loading : NetworkState()
    data class Loaded(val itemsCount: Int) : NetworkState()
    data class Error(val message: String?, val isConnected: Boolean) : NetworkState()
}