package com.koalatea.sedaily.repository

import android.content.SharedPreferences

private const val KEY_TOKEN = "token-key"

class SessionRepository(
        private val sharedPreferences: SharedPreferences
) {

    var token: String? = sharedPreferences.getString(KEY_TOKEN, "")
        set(value) {
            field = value

            val editor = sharedPreferences.edit()
            editor.putString(KEY_TOKEN, value)
            editor.apply()
        }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

}
