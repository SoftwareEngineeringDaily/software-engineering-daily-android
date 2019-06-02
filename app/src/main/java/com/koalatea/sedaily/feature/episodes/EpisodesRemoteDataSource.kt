package com.koalatea.sedaily.feature.episodes

import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodesRemoteDataSource(private val api: SEDailyApi) {

    suspend fun getPosts(searchQuery: SearchQuery, createdAtBefore: String? = null) = withContext(Dispatchers.IO) {
        api.getPostsAsync(searchQuery.searchTerm, searchQuery.categoryId, createdAtBefore).await()
    }

}