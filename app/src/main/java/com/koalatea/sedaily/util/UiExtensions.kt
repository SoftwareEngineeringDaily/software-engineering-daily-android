package com.koalatea.sedaily.util

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    return activityManager?.getRunningServices(Integer.MAX_VALUE)?.any { it.service.className == serviceClassName } ?: false
}

val Fragment.supportActionBar
    get() = (activity as? AppCompatActivity)?.supportActionBar

fun NavController.setupActionBar(activity: AppCompatActivity, appBarConfig: AppBarConfiguration) =
        activity.setupActionBarWithNavController(this, appBarConfig)