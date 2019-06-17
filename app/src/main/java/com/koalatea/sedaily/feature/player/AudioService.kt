package com.koalatea.sedaily.feature.player

import android.app.Notification
import android.app.PendingIntent
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
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
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
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.Listened
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val PLAYBACK_CHANNEL_ID = "playback_channel"
private const val PLAYBACK_NOTIFICATION_ID = 1
private const val MEDIA_SESSION_TAG = "sed_audio"

private const val ARG_EPISODE_ID = "episode_id"
private const val ARG_URI = "uri_string"
private const val ARG_TITLE = "title"
private const val ARG_START_POSITION = "start_position"
private const val ARG_AUTO_PLAY = "auto_play"

private const val DEFAILT_AUTO_PLAY = true

class AudioService : LifecycleService() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService
    }

    companion object {

        fun newIntent(context: Context, episode: Episode? = null, autoPlay: Boolean = DEFAILT_AUTO_PLAY) = Intent(context, AudioService::class.java).apply {
            episode?.let {
                putExtra(ARG_EPISODE_ID, episode._id)
                episode.titleString?.let { title -> putExtra(ARG_TITLE, title) }
                episode.uriString?.let{ uriString -> putExtra(ARG_URI, Uri.parse(uriString)) }
                putExtra(ARG_START_POSITION, episode.startPosition)
            }

            putExtra(ARG_AUTO_PLAY, autoPlay)
        }

    }

    private val appDatabase: AppDatabase by inject()

    var episodeId: String? = null
        private set

    val isPlaying
        get() = exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady

    lateinit var exoPlayer: SimpleExoPlayer
        private set

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)

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
        saveLastListeningPosition()

        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        playerNotificationManager?.setPlayer(null)

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
        exoPlayer.playWhenReady = true

//        // Variable speed playback https://medium.com/google-exoplayer/variable-speed-playback-with-exoplayer-e6e6a71e0343
////        exoPlayer.setPlaybackParameters
    }

    private fun handleIntent(intent: Intent?) {
        episodeId = intent?.getStringExtra(ARG_EPISODE_ID)

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                applicationContext,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                object : PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): String {
                        return intent?.getStringExtra(ARG_TITLE) ?: getString(R.string.loading_dots)
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
        playerNotificationManager?.setMediaSessionToken(mediaSession?.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                    val bitmap = getBitmapFromVectorDrawable(applicationContext, R.drawable.vd_notification_icon)
                    val extras = Bundle().apply {
                        putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                    }

                    val title = intent?.getStringExtra(ARG_TITLE) ?: getString(R.string.loading_dots)

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
            if (intent.getBooleanExtra(ARG_AUTO_PLAY, DEFAILT_AUTO_PLAY)) {
                intent.getParcelableExtra<Uri>(ARG_URI)?.also { uri ->
                    val startPosition = intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())

                    play(uri, startPosition)
                }
            }
        }
    }

    private fun saveLastListeningPosition() = lifecycleScope.launch {
            episodeId?.let { appDatabase.listenedDao().insert(Listened(it, exoPlayer.contentPosition, exoPlayer.duration)) }
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
            Log.e("ZZZ", "Playback state :: $playbackState, contentPosition :: ${exoPlayer.contentPosition}, isPlaying :: $isPlaying")
            if (playbackState == Player.STATE_READY) {
                // Paused
                if (!exoPlayer.playWhenReady) {
                    saveLastListeningPosition()
                }
            }
        }

        override fun onPlayerError(e: ExoPlaybackException?) {
            Log.e("ZZZ", "Error :: $e")
        }

    }

}