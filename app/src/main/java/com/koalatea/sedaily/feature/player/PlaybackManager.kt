package com.koalatea.sedaily.feature.player

import android.content.SharedPreferences

private const val KEY_PLAYBACK_SPEED = "playback_speed"

class PlaybackManager(
        private val sharedPreferences: SharedPreferences
) {

    var playbackSpeed: Float = sharedPreferences.getFloat(KEY_PLAYBACK_SPEED, 1f)
        set(value) {
            field = value

            val editor = sharedPreferences.edit()
            editor.putFloat(KEY_PLAYBACK_SPEED, value)
            editor.apply()
        }
}