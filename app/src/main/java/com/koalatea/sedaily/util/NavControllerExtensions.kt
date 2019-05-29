package com.koalatea.sedaily.util

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

fun NavController.setupActionBar(activity: AppCompatActivity, appBarConfig: AppBarConfiguration) = activity.setupActionBarWithNavController(this, appBarConfig)
