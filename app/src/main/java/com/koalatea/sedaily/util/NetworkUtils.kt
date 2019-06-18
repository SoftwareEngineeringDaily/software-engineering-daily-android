package com.koalatea.sedaily.util

import retrofit2.Response

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Response<T>? = try {
        call()
    } catch (e: Throwable) {
        null
    }
