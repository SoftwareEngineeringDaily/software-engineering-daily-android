package com.koalatea.sedaily.network

import okhttp3.ResponseBody
import java.io.IOException

sealed class Result<out T: Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class ErrorWithCache<out T : Any>(val exception: Exception, val cachedData: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

fun ResponseBody?.toException() = IOException(this?.string() ?: "Error occurred")