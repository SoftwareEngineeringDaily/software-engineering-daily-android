package com.koalatea.sedaily.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.koalatea.sedaily.R
import com.koalatea.sedaily.analytics.AnalyticsConstants
import com.koalatea.sedaily.network.Resource
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment() {

    private val firebaseAnalytics: FirebaseAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics.setCurrentScreen(requireActivity(), this@BaseFragment::class.java.simpleName, null)

//        if (savedInstanceState == null) {
//            firebaseAnalytics.logEvent(AnalyticsConstants.EVENT_SCREEN_VIEW, Bundle().apply {
//                putString(AnalyticsConstants.KEY_SCREEN_NAME, this@BaseFragment::class.java.simpleName)
//            })
//
////            FirebaseAnalytics.Param.
//        }
    }

    protected fun acknowledgeGenericError()
            = Snackbar.make(requireView(), R.string.error_generic, Snackbar.LENGTH_SHORT).show()
    protected fun acknowledgeConnectionError()
            = Snackbar.make(requireView(), R.string.error_not_connected, Snackbar.LENGTH_SHORT).show()

    protected fun acknowledgeError(message: String)
            = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

    protected fun acknowledgeResourceError(error: Resource.Error) {
        if (!error.isConnected) {
            acknowledgeConnectionError()
        } else {
            error.exception.message?.let { acknowledgeError(it) } ?: acknowledgeGenericError()
        }
    }

}