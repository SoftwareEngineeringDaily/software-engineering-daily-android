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
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
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
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.database.model.Listened
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*

private const val PLAYBACK_CHANNEL_ID = "playback_channel"
private const val PLAYBACK_NOTIFICATION_ID = 1
private const val MEDIA_SESSION_TAG = "sed_audio"

private const val PLAYBACK_TIMER_DELAY = 5 * 1000L

private const val ARG_EPISODE_ID = "episode_id"
private const val ARG_URI = "uri_string"
private const val ARG_TITLE = "title"
private const val ARG_START_POSITION = "start_position"
private const val ARG_PLAYBACK_SPEED = "playback_speed"

class AudioService : LifecycleService() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService
    }

    companion object {

        @MainThread
        fun newIntent(context: Context, episodeDetails: EpisodeDetails? = null, playbackSpeed: Float = 1f) = Intent(context, AudioService::class.java).apply {
            episodeDetails?.let {
                val episode = episodeDetails.episode

                putExtra(ARG_EPISODE_ID, episode._id)
                episode.titleString?.let { title -> putExtra(ARG_TITLE, title) }
                episode.uriString?.let{ uriString -> putExtra(ARG_URI, Uri.parse(uriString)) }
                putExtra(ARG_START_POSITION, episodeDetails.listened?.startPosition)
                putExtra(ARG_PLAYBACK_SPEED, playbackSpeed)
            }
        }

    }

    private val appDatabase: AppDatabase by inject()

    private var playbackTimer: Timer? = null

    var episodeId: String? = null
        private set

    // FIXME :: Make it private
    lateinit var exoPlayer: SimpleExoPlayer
        private set

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus>()
    val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)

        handleIntent(intent)

        return AudioServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_SPEECH)
                .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)

        exoPlayer.addListener(PlayerEventListener())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        cancelPlaybackMonitor()

        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        playerNotificationManager?.setPlayer(null)

        exoPlayer.release()

        super.onDestroy()
    }

    @MainThread
    fun play(uri: Uri, startPosition: Long, playbackSpeed: Float? = null) {
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

        playbackSpeed?.let { changePlaybackSpeed(playbackSpeed) }

        exoPlayer.prepare(mediaSource, !haveStartPosition, false)
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun changePlaybackSpeed(playbackSpeed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    @MainThread
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
                        return getBitmapFromVectorDrawable(applicationContext, R.drawable.vd_sed_icon)
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
                    val bitmap = getBitmapFromVectorDrawable(applicationContext, R.drawable.vd_sed_icon)
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
            intent.getParcelableExtra<Uri>(ARG_URI)?.also { uri ->
                val startPosition = intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())
                val playbackSpeed = intent.getFloatExtra(ARG_PLAYBACK_SPEED, 1f)

                play(uri, startPosition, playbackSpeed)
            }
        }
    }

    @MainThread
    private fun saveLastListeningPosition() = lifecycleScope.launch {
        episodeId?.let { appDatabase.listenedDao().insert(Listened(it, exoPlayer.contentPosition, exoPlayer.duration)) }
    }

    @MainThread
    private fun monitorPlaybackProgress() {
        if (playbackTimer == null) {
            playbackTimer = Timer()

            playbackTimer?.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            saveLastListeningPosition()

                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    if (exoPlayer.duration - exoPlayer.contentPosition <= PLAYBACK_TIMER_DELAY) {
                                        playbackTimer?.cancel()
                                    }
                                }
                            }
                        }
                    },
                    PLAYBACK_TIMER_DELAY,
                    PLAYBACK_TIMER_DELAY)
        }
    }

    @MainThread
    private fun cancelPlaybackMonitor() {
        saveLastListeningPosition()

        playbackTimer?.cancel()
        playbackTimer = null
    }

    @MainThread
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
            if (playbackState == Player.STATE_READY) {
                if (exoPlayer.playWhenReady) {
                    episodeId?.let { _playerStatusLiveData.value = PlayerStatus.Playing(it) }

                    monitorPlaybackProgress()
                } else {// Paused
                    episodeId?.let { _playerStatusLiveData.value = PlayerStatus.Paused(it) }

                    cancelPlaybackMonitor()
                }
            } else {
                episodeId?.let { _playerStatusLiveData.value = PlayerStatus.Other(it) }
            }
        }

        override fun onPlayerError(e: ExoPlaybackException?) {
            episodeId?.let { _playerStatusLiveData.value = PlayerStatus.Error(it, e) }
        }

    }

}