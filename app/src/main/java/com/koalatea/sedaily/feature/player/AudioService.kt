package com.koalatea.sedaily.feature.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.koalatea.sedaily.BuildConfig
import com.koalatea.sedaily.MainActivity
import com.koalatea.sedaily.R

private const val PLAYBACK_CHANNEL_ID = "playback_channel"
private const val PLAYBACK_NOTIFICATION_ID = 1
private const val MEDIA_SESSION_TAG = "sed_audio"

private const val ARG_URI = "uriString"
private const val ARG_TITLE = "title"
private const val ARG_START_POSITION = "start_position"

class AudioService : Service() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService
    }

    companion object {

        fun newIntent(context: Context, title: String? = null, uriString: String? = null, startPosition: Long = 0) = Intent(context, AudioService::class.java).apply {
            title?.let { putExtra(ARG_TITLE, title) }
            uriString?.let {
                putExtra(ARG_URI, Uri.parse(uriString))
                putExtra(ARG_START_POSITION, startPosition)
            }
        }

    }

    lateinit var exoPlayer: SimpleExoPlayer
        private set

    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var autoPlayStateSet = false

    override fun onBind(intent: Intent?): IBinder {
        handleIntent(intent)

        return AudioServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        exoPlayer.addListener(PlayerEventListener())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
        playerNotificationManager.setPlayer(null)

        exoPlayer.release()

        super.onDestroy()
    }

    fun play(uri: Uri, startPosition: Long) {
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

    private fun handleIntent(intent: Intent?) {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                applicationContext,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                object : PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): String {
                        return intent?.getStringExtra(ARG_TITLE) ?: ""
                    }

                    @Nullable
                    override fun createCurrentContentIntent(player: Player): PendingIntent? = PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(applicationContext, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT)

                    @Nullable
                    override fun getCurrentContentText(player: Player): String? {
                        return null
                    }

                    @Nullable
                    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                        return getBitmapFromVectorDrawable(applicationContext, R.drawable.vd_notification_icon)
                    }
                },
                object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                        startForeground(notificationId, notification)
                    }

                    override fun onNotificationCancelled(notificationId: Int) {
                        stopSelf()
                    }
                }
        ).apply {
            // omit skip previous and next actions
            setUseNavigationActions(false)

            val incrementMs = resources.getInteger(R.integer.increment_ms).toLong()
            setFastForwardIncrementMs(incrementMs)
            setRewindIncrementMs(incrementMs)

            setPlayer(exoPlayer)
        }

        // Show lock screen controls and let apps like Google assistant manager playback.
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG).apply {
            isActive = true
        }
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                    val bitmap = getBitmapFromVectorDrawable(applicationContext, R.drawable.vd_notification_icon)
                    val extras = Bundle().apply {
                        putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                    }

                    val title = intent?.getStringExtra(ARG_TITLE) ?: ""

                    return MediaDescriptionCompat.Builder()
                            .setIconBitmap(bitmap)
                            .setTitle(title)
                            .setExtras(extras)
                            .build()
                }
            })

            setPlayer(exoPlayer)
        }

        // Play
        intent?.let {
            intent.getParcelableExtra<Uri>(ARG_URI)?.also { uri ->
                val startPosition = intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())

                play(uri, startPosition)
            }
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()

            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        }
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