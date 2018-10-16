package com.koalatea.sedaily.auth

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.koalatea.sedaily.SEDApp

class UserRepository private constructor(context: Context) {
    private var token: String? = null
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        this.token = preferences.getString(TOKEN_KEY, "")
    }

    fun setToken(token: String) {
        this.token = token

        val editor = preferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(): String? {
        return this.token
    }

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            val context: Context = SEDApp.appContext!!

            if (instance == null) {
                instance = UserRepository(context)
            }
            return instance as UserRepository
        }

        const val TOKEN_KEY = "token-key"
    }
}
