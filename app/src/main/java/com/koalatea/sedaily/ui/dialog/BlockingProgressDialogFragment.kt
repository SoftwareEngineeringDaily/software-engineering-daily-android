package com.koalatea.sedaily.ui.dialog

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

private const val ARG_MESSAGE = "message"

class BlockingProgressDialogFragment : AppCompatDialogFragment() {

    companion object {

        fun show(fragmentManager: FragmentManager, message: String, tag: String) {
            val dialogFragment = fragmentManager.findFragmentByTag(tag)
            if (dialogFragment == null) {

                val fragment = BlockingProgressDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_MESSAGE, message)
                    }
                }

                fragment.show(fragmentManager, tag)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = requireArguments().getString(ARG_MESSAGE)

        return ProgressDialog(activity, theme).apply {
            setMessage(message)
            isIndeterminate = true
        }
    }

}
