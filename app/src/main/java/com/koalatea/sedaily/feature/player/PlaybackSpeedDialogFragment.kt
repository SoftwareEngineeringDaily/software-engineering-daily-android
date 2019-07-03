package com.koalatea.sedaily.feature.player

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.koalatea.sedaily.R

class PlaybackSpeedDialogFragment : AppCompatDialogFragment() {

    interface OnPlaybackChangedListener {
        fun onPlaybackSpeedChanged(playbackSpeed: Float)
    }

    companion object {

        fun show(targetFragment: Fragment, fragmentManager: FragmentManager, tag: String) {
            val dialogFragment = fragmentManager.findFragmentByTag(tag)
            if (dialogFragment == null) {
                val dialog = PlaybackSpeedDialogFragment()
                dialog.setTargetFragment(targetFragment, 0)

                dialog.show(fragmentManager, tag)
            }
        }

    }

    private lateinit var listener: OnPlaybackChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            targetFragment is OnPlaybackChangedListener -> this.listener = targetFragment as OnPlaybackChangedListener
            activity is OnPlaybackChangedListener -> this.listener = activity as OnPlaybackChangedListener
            else -> throw IllegalArgumentException("Activity: $activity, or target fragment: $targetFragment doesn't implement ${OnPlaybackChangedListener::class.java.name}")
        }

        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), theme).apply {
            setTitle(R.string.playback_speed_title)

            setItems(R.array.playback_speed_options) { _, which ->
                run {
                    val speed = context.resources.getStringArray(R.array.playback_speed_options)[which].toFloat()

                    listener.onPlaybackSpeedChanged(speed)
                }
            }
        }.create()
    }

}