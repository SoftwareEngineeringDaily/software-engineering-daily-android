package com.koalatea.sedaily.ui.fragment

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R

abstract class BaseFragment : Fragment() {

    protected fun acknowledgeGenericError()
            = Snackbar.make(requireView(), R.string.error_generic, Snackbar.LENGTH_SHORT).show()
    protected fun acknowledgeConnectionError()
            = Snackbar.make(requireView(), R.string.error_not_connected, Snackbar.LENGTH_SHORT).show()

    protected fun acknowledgeError(message: String)
            = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

}