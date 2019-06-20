package com.koalatea.sedaily.network

import okhttp3.ResponseBody
import java.io.IOException

sealed class Resource<out T: Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>()
    object Loading : Resource<Nothing>()
    data class Error(val exception: Exception, val isConnected: Boolean) : Resource<Nothing>()
}

fun ResponseBody?.toException() = IOException(this?.string() ?: "Error occurred")