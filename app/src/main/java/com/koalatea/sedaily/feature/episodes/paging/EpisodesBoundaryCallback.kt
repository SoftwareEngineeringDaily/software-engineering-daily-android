/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koalatea.sedaily.feature.episodes.paging

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodesBoundaryCallback(
        private val searchQuery: SearchQuery,
        private val api: SEDailyApi,
        private val insertResultIntoDb: (SearchQuery, List<Episode>?) -> Unit,
        private val handleSuccessfulRefresh: (SearchQuery, List<Episode>?) -> Unit,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Episode>() {

    val networkState = MutableLiveData<NetworkState>()
    val refreshState = MutableLiveData<NetworkState>()

    private var isRequestInProgress = false

    @MainThread
    override fun onZeroItemsLoaded() {
        load(callback = insertResultIntoDb)
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: Episode) {
        // ignored, since we only ever append to what's in the DB
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Episode) {
        load(itemAtEnd.date, insertResultIntoDb)
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
            networkState.value = NetworkState.Loading
            if (createdAtBefore == null) {// First request.
                refreshState.value = NetworkState.Loading
            }

            val response = api.getPostsAsync(searchQuery.searchTerm, searchQuery.categoryId, createdAtBefore, networkPageSize).await()
            if (response.isSuccessful) {
                callback(searchQuery, response.body())

                networkState.value = NetworkState.Loaded(response.body()?.size ?: 0)
                if (createdAtBefore == null) {
                    refreshState.value = NetworkState.Loaded(response.body()?.size ?: 0)
                }
            } else {
                val error = NetworkState.Error(response.errorBody()?.string() ?: "Unknown error")

                networkState.value = error
                if (createdAtBefore == null) {
                    refreshState.value = error
                }
            }

            isRequestInProgress  = false
        }
    }


}