package com.koalatea.sedaily.network

import com.google.gson.Gson
import com.koalatea.sedaily.database.model.Thread
import okhttp3.ResponseBody
import java.io.IOException

sealed class Resource<out T: Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>()
    object Loading : Resource<Nothing>()
    data class Error(val exception: Exception, val isConnected: Boolean) : Resource<Nothing>()
}

inline fun <reified T> ResponseBody?.toObject(gson: Gson) = this?.string()?.let { gson.fromJson(it, T::class.java) }

fun ResponseBody?.toException() = IOException(this?.string())