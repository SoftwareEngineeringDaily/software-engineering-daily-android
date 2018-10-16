package com.koalatea.sedaily

import android.app.Application
import android.content.Context
import br.com.bemobi.medescope.Medescope
import com.google.firebase.analytics.FirebaseAnalytics

class SEDApp : Application() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate() {
        super.onCreate()
        SEDApp.appContext = getApplicationContext()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Medescope.getInstance(this).setApplicationName("My Application Name")
    }

    companion object {

        var appContext: Context? = null
            private set
    }
}