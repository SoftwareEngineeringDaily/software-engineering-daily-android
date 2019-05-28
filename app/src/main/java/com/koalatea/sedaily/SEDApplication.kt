package com.koalatea.sedaily

import android.app.Application
import android.content.Context
import com.koalatea.sedaily.koin.module.appModule
import com.koalatea.sedaily.koin.module.networkModule
import com.koalatea.sedaily.koin.module.viewHoldersModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

open class SEDApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin{
            androidLogger()// FIXME :: Should this be in a production build.
            androidContext(this@SEDApplication)
            modules(listOf(appModule, networkModule, viewHoldersModule))
        }

        appContext = applicationContext
    }

    companion object {

        @Deprecated("Do not use")
        var appContext: Context? = null
            private set
    }
}