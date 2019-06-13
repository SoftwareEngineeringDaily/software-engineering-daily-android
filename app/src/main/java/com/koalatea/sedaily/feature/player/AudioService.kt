package com.koalatea.sedaily.feature.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.koalatea.sedaily.BuildConfig

private const val ARG_URI = "uri"
private const val ARG_START_POSITION = "start_position"

class AudioService : Service() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService
    }

    lateinit var exoPlayer: SimpleExoPlayer
        private set

    private var autoPlayStateSet = false

    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            intent.getParcelableExtra<Uri>(ARG_URI).also { uri ->
                val startPosition = intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())

                playMedia(uri, startPosition)
            }
        }

        return AudioServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        exoPlayer.addListener(PlayerEventListener())
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        intent?.let {
//            intent.getParcelableExtra<Uri>(ARG_URI).also { uri ->
//                val startPosition = intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())
//
//                playMedia(uri, startPosition)
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.release()
//        playerNotificationManager.setPlayer(null)
    }

    fun playMedia(uri: Uri, startPosition: Long) {
        val userAgent = Util.getUserAgent(applicationContext, BuildConfig.APPLICATION_ID)
        val mediaSource = ExtractorMediaSource(
                uri,
                DefaultDataSourceFactory(applicationContext, userAgent),
                DefaultExtractorsFactory(),
                null,
                null)

        val haveStartPosition = startPosition != C.POSITION_UNSET.toLong()
        if (haveStartPosition) {
            exoPlayer.seekTo(startPosition)
        }

        exoPlayer.prepare(mediaSource, !haveStartPosition, false)

//        // Variable speed playback https://medium.com/google-exoplayer/variable-speed-playback-with-exoplayer-e6e6a71e0343
////        exoPlayer.setPlaybackParameters
    }

    private fun showNotification() {
//        val CHANNEL_ID = "Channel"
//        val NOTIFICATION_ID = 200098
//        // https://medium.com/google-exoplayer/playback-notifications-with-exoplayer-a2f1a18cf93b
////        NotificationCompat.Builder
//        playerNotificationManager = PlayerNotificationManager(context, CHANNEL_ID, NOTIFICATION_ID, DescriptionAdapter())
//        // omit skip previous and next actions
//        playerNotificationManager.setUseNavigationActions(false)
////        // omit fast forward action by setting the increment to zero
////        playerNotificationManager.setFastForwardIncrementMs(0)
////        // omit rewind action by setting the increment to zero
////        playerNotificationManager.setRewindIncrementMs(0)
////        // omit the stop action
////        playerNotificationManager.setStopAction(null)

//        playerNotificationManager.setPlayer(exoPlayer)
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.e("ZZZ", "Playback state :: $playbackState")
            if (playbackState == Player.STATE_READY) {
                if (!autoPlayStateSet) {
                    exoPlayer.playWhenReady = true
                    autoPlayStateSet = true
                }
            }
        }

        override fun onPlayerError(e: ExoPlaybackException?) {
            Log.e("ZZZ", "Error :: $e")
        }
    }

}