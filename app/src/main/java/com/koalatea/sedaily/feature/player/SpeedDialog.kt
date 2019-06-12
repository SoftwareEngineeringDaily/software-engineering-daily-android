package com.koalatea.sedaily.feature.player

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.koalatea.sedaily.R
import org.koin.android.ext.android.inject

class SpeedDialog : DialogFragment() {

    private val podcastSessionStateManager: PodcastSessionStateManager by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder.setTitle(R.string.speed)
                .setItems(R.array.speed_options) { _, which ->
                    run {
                        podcastSessionStateManager.currentSpeed = which
                    }
                }

        return builder.create()
    }
}