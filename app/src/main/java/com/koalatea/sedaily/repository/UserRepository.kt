package com.koalatea.sedaily.repository

import android.content.SharedPreferences
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TOKEN_KEY = "token-key"

class UserRepository(
        private val api: SEDailyApi,
        private val preferences: SharedPreferences
) {

    var token: String? = preferences.getString(TOKEN_KEY, "")
        set(value) {
            field = value

            val editor = preferences.edit()
            editor.putString(TOKEN_KEY, token)
            editor.apply()
        }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    suspend fun login(username: String, password: String, email: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.loginAsync(username, email, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(response?.errorBody().toException())
        }
    }

    suspend fun register(username: String, password: String, email: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.registerAsync(username, email, password).await() }
        val user = response?.body()
        if (response?.isSuccessful == true && user != null) {
            Resource.Success(user)
        } else {
            Resource.Error(response?.errorBody().toException())
        }
    }

}
