package com.koalatea.sedaily.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

private const val ARG_MESSAGE = "message"
private const val ARG_POSITIVE_BUTTON = "positive_button"
private const val ARG_NEGATIVE_BUTTON = "negative_button"

class AlertDialogFragment : AppCompatDialogFragment() {

    companion object {

        fun show(
                fragmentManager: FragmentManager,
                message: String,
                positiveButton: String?,
                negativeButton: String? = null,
                tag: String) {
            val dialogFragment = fragmentManager.findFragmentByTag(tag)
            if (dialogFragment == null) {

                val fragment = AlertDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_MESSAGE, message)
                        putString(ARG_POSITIVE_BUTTON, positiveButton)
                        putString(ARG_NEGATIVE_BUTTON, negativeButton)
                    }
                }

                fragment.show(fragmentManager, tag)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val message = args.getString(ARG_MESSAGE)
        val positiveButton = args.getString(ARG_POSITIVE_BUTTON)
        val negativeButton = args.getString(ARG_NEGATIVE_BUTTON)

        return AlertDialog.Builder(requireContext())
                .apply {
                    setMessage(message)

                    positiveButton?.let {
                        setPositiveButton(it) { dialog, _ -> dialog.dismiss() }
                    }

                    negativeButton?.let {
                        setNegativeButton(it) { dialog, _ -> dialog.dismiss() }
                    }
                }
                .create()
    }

}
