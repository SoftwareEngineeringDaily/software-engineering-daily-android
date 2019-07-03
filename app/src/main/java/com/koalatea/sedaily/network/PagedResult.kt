package com.koalatea.sedaily.network

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

// Inspired by https://github.com/googlesamples/android-architecture-components/blob/corotuine-network-bound-resource/PagingWithNetworkSample/app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/Listing.kt
data class PagedResult<T>(
        // The LiveData of paged lists for the UI to observe
        val pagedList: LiveData<PagedList<T>>,
        // Represents the network request status to show to the user
        val networkState: LiveData<NetworkState>,
        // Represents the refresh status to show to the user. Separate from networkState, this
        // value is importantly only when refresh is requested.
        val refreshState: LiveData<NetworkState>,
        // Refreshes the whole data and fetches it from scratch.
        val refresh: () -> Unit
)

sealed class NetworkState {
    object Loading : NetworkState()
    data class Loaded(val itemsCount: Int) : NetworkState()
    data class Error(val message: String?, val isConnected: Boolean) : NetworkState()
}