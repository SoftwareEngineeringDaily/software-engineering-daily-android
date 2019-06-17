package com.koalatea.sedaily.util

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

val Fragment.supportActionBar
    get() = (activity as? AppCompatActivity)?.supportActionBar

@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    return activityManager?.getRunningServices(Integer.MAX_VALUE)?.any { it.service.className == serviceClassName } ?: false
}