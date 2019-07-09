package com.koalatea.sedaily

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class ReleaseSEDApplication : SEDApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(CrashReportingTree())
    }

    private inner class CrashReportingTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, e: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            Crashlytics.log(priority, if (tag.isNullOrBlank()) "SEDaily" else tag, message)

            if (e != null) {
                if (priority == Log.ERROR) {
                    Crashlytics.logException(e)
                } else if (priority == Log.WARN) {
                    Crashlytics.logException(e)
                }
            }
        }
    }

}