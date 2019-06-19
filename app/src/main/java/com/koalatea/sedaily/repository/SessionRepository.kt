package com.koalatea.sedaily.repository

import android.content.SharedPreferences

private const val TOKEN_KEY = "token-key"

class SessionRepository(
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

}
