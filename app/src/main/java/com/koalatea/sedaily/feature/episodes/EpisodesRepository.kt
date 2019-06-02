package com.koalatea.sedaily.feature.episodes

import com.koalatea.sedaily.database.EpisodeDao
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.toException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodesRepository(
        private val remoteDataSource: EpisodesRemoteDataSource,
        private val localDataSource: EpisodeDao) {

    suspend fun fetchPosts(searchQuery: SearchQuery, createdAtBefore: String? = null) = withContext(Dispatchers.IO) {
        val response = remoteDataSource.getPosts(searchQuery, createdAtBefore)
        val isInitialRequest = createdAtBefore.isNullOrBlank()
        if (response.isSuccessful) {
            val episodes = response.body()

            // Only cache the first page when searching for all podcasts.
            if (isInitialRequest) {
                if (episodes != null) {
                    localDataSource.clearTable()

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