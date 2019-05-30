package com.koalatea.sedaily.feature.episodes

import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.toException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodesRepository(
        private val remoteDataSource: EpisodesRemoteDataSource,
        private val localDataSource: EpisodeDao) {

    suspend fun fetchPosts(searchTerm: String? = null, categoryId: String? = null, createdAtBefore: String = "") = withContext(Dispatchers.IO) {

        fun isInitialRequest(searchTerm: String? = null, categoryId: String? = null, createdAtBefore: String = "") = searchTerm.isNullOrBlank() && categoryId.isNullOrBlank() && createdAtBefore.isBlank()

        val response = remoteDataSource.getPosts(searchTerm, categoryId, createdAtBefore)
        val isInitialRequest = isInitialRequest(searchTerm, categoryId, createdAtBefore)
        if (response.isSuccessful) {
            val episodes = response.body()

            // Only cache the first page when searching for all podcasts.
            if (isInitialRequest) {
                if (episodes != null) {
                    localDataSource.insert(*episodes.toTypedArray())
                } else {
                    localDataSource.clearTable()
                }
            }

            return@withContext Result.Success(episodes ?: listOf())
        } else {
            if (isInitialRequest) {
                return@withContext Result.ErrorWithCache(response.errorBody().toException(), localDataSource.all)
            }

            return@withContext Result.Error(response.errorBody().toException())
        }
    }

}