package com.koalatea.sedaily.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkInfo
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Response<T>? = try {
    call()
} catch (e: Throwable) {
    null
}

private val Context.networkInfo: NetworkInfo?
    get() = (getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo

val Context.isConnected: Boolean
    get() = networkInfo?.isConnected ?: false

private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd\'T\'HH:mm:ss"
fun String.toUTCDate(format: String = DEFAULT_DATE_FORMAT): Date? {
    return try {
        val inputFormat = SimpleDateFormat(format, Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

        return inputFormat.parse(this)
    } catch (e: Exception) {
        Timber.w(e)
        null
    }
}
