package com.koalatea.sedaily

import android.app.Application
import android.content.Context
import br.com.bemobi.medescope.Medescope
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.leakcanary.LeakCanary

class SEDApp : Application() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        SEDApp.appContext = applicationContext
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Medescope.getInstance(this).setApplicationName("My Application Name")
    }

    companion object {

        var appContext: Context? = null
            private set
    }
}