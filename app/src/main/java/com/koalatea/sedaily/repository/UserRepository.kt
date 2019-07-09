package com.koalatea.sedaily.repository

import com.google.gson.Gson
import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.response.ErrorResponse
import com.koalatea.sedaily.network.toObject
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UserRepository(
        private val api: SEDailyApi,
        private val networkManager: NetworkManager,
        private val gson: Gson,
        private val sessionRepository: SessionRepository,
        private val episodesRepository: EpisodesRepository
) {

    suspend fun fetchProfile() = withContext(Dispatchers.IO) {
        if (sessionRepository.isLoggedIn) {
            val response = safeApiCall { api.getProfileAsync().await() }
            val profile = response?.body()
            if (response?.isSuccessful == true && profile != null) {
                Resource.Success(profile)
            } else {
                Resource.Error(
                        IOException(response?.errorBody().toObject<ErrorResponse>(gson)?.message),
                        networkManager.isConnected)
            }
        } else {
            Resource.RequireLogin
        }
    }

    suspend fun login(usernameOrEmail: String, password: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.loginAsync(usernameOrEmail, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(
                    IOException(response?.errorBody().toObject<ErrorResponse>(gson)?.message),
                    networkManager.isConnected)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        sessionRepository.token = ""

        // Clear DB tables
        episodesRepository.clear()
    }

    suspend fun register(username: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.registerAsync(username, email, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(
                    IOException(response?.errorBody().toObject<ErrorResponse>(gson)?.message),
                    networkManager.isConnected)
        }
    }

}
