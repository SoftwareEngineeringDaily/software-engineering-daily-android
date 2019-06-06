package com.koalatea.sedaily.network

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Result<T>(
        // the LiveData of paged lists for the UI to observe
        val pagedList: LiveData<PagedList<T>>,
        // represents the network request status to show to the user
        val networkState: LiveData<NetworkState>,
        // represents the refresh status to show to the user. Separate from networkState, this
        // value is importantly only when refresh is requested.
        val refreshState: LiveData<NetworkState>,
        // refreshes the whole data and fetches it from scratch.
        val refresh: () -> Unit
)

sealed class NetworkState {
    object Loading : NetworkState()
    data class Loaded(val itemsCount: Int) : NetworkState()
    data class Error(val message: String) : NetworkState()
}