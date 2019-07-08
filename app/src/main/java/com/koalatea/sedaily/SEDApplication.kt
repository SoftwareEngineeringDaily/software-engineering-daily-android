package com.koalatea.sedaily

import android.app.Application
import android.content.Context
import com.koalatea.sedaily.koin.module.appModule
import com.koalatea.sedaily.koin.module.networkModule
import com.koalatea.sedaily.koin.module.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

open class SEDApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin{
            androidLogger()
            androidContext(this@SEDApplication)
            modules(listOf(appModule, networkModule, viewModelsModule))
        }
    }

}