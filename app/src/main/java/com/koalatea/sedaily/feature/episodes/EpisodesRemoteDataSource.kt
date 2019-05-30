package com.koalatea.sedaily.feature.episodes

import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodesRemoteDataSource(private val api: SEDailyApi) {

    suspend fun getPosts(searchTerm: String? = null, categoryId: String? = null, createdAtBefore: String = "") = withContext(Dispatchers.IO) {
        api.getPostsAsync(searchTerm, categoryId, createdAtBefore).await()
    }

}