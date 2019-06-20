package com.koalatea.sedaily.repository

import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
        private val api: SEDailyApi,
        private val networkManager: NetworkManager
) {

    suspend fun login(usernameOrEmail: String, password: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.loginAsync(usernameOrEmail, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

    suspend fun register(username: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.registerAsync(username, email, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

}
