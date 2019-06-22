package com.koalatea.sedaily.repository

import com.google.gson.Gson
import com.koalatea.sedaily.network.*
import com.koalatea.sedaily.network.response.ErrorResponse
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UserRepository(
        private val api: SEDailyApi,
        private val networkManager: NetworkManager,
        private val gson: Gson
) {

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
