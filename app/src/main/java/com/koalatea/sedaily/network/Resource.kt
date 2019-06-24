package com.koalatea.sedaily.network

import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.IOException

sealed class Resource<out T: Any> {
    object Loading : Resource<Nothing>()
    object RequireLogin : Resource<Nothing>()
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val exception: Exception, val isConnected: Boolean) : Resource<Nothing>()
}

inline fun <reified T> ResponseBody?.toObject(gson: Gson) = this?.string()?.let { gson.fromJson(it, T::class.java) }

fun ResponseBody?.toException() = IOException(this?.string())