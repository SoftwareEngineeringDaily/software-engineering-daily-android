package com.koalatea.sedaily.repository

import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsRepository(
        private val api: SEDailyApi,
        private val networkManager: NetworkManager,
        private val sessionRepository: SessionRepository
) {

    suspend fun fetchComments(entityId: String) = withContext(Dispatchers.IO) {
        if (sessionRepository.isLoggedIn) {
            val response = safeApiCall { api.getEpisodeCommentsAsync(entityId).await() }
            val comments = response?.body()
            if (response?.isSuccessful == true && comments != null) {
                Resource.Success(comments.result)
            } else {
                Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
            }
        } else {
            Resource.RequireLogin
        }
    }

}