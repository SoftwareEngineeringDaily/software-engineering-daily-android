package com.koalatea.sedaily.feature.playbar

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.koalatea.sedaily.R

class SpeedDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder.setTitle(R.string.speed)
                .setItems(R.array.speed_options) { _, which ->
                    run {
                        PodcastSessionStateManager.getInstance().currentSpeed = which
                    }
                }

        return builder.create()
    }
}