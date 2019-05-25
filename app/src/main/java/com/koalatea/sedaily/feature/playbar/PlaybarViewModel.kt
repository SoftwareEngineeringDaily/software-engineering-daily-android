package com.koalatea.sedaily.feature.playbar

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel

class PlaybarViewModel : ViewModel() {
    fun sendSpeedChangeIntent(currentSpeed: Int, activity: Activity) {
        val controller = MediaControllerCompat.getMediaController(activity)
        if (controller != null) {
            val args = Bundle()
            args.putInt("SPEED", currentSpeed)
            // @TODO: Make constant
            controller.transportControls.sendCustomAction("SPEED_CHANGE", args)
        }
    }

    fun setListenedProgress(mLastPlaybackState: PlaybackStateCompat): Long {
        var currentPosition = mLastPlaybackState.position
        if (mLastPlaybackState.state == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState.lastPositionUpdateTime
            currentPosition += (timeDelta.toInt() * mLastPlaybackState.playbackSpeed).toLong()
        }

        return currentPosition
    }

    fun playPause(controller: MediaControllerCompat) {
        val stateObj = controller.playbackState
        val state = stateObj?.state ?: PlaybackStateCompat.STATE_NONE

        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
            playMedia(controller)
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            pauseMedia(controller)
        }
    }

    private fun playMedia(controller: MediaControllerCompat?) {
        controller?.transportControls?.play()
    }

    private fun pauseMedia(controller: MediaControllerCompat?) {
        controller?.transportControls?.pause()
    }

    fun back15(activity: Activity) {
        movePlayback(activity, "MOVE_BACK")
    }

    fun skip15(activity: Activity) {
        movePlayback(activity, "MOVE_FORWARD")
    }

    private fun movePlayback(activity: Activity, action: String) {
        val controller = MediaControllerCompat.getMediaController(activity) ?: return

        val args = Bundle()
        args.putInt("DISTANCE", 15000)
        // @TODO: Make constant
        controller.transportControls.sendCustomAction(action, args)

    }
}