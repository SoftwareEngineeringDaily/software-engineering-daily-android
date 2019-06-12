package com.koalatea.sedaily.feature.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.koalatea.sedaily.feature.player.media.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

@SuppressLint("Registered")
open class PlaybackActivity : AppCompatActivity() {

    private val podcastSessionStateManager: PodcastSessionStateManager by inject()

    private var mMediaBrowser: MediaBrowserCompat? = null
    private val mConnectionCallbacks: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                connectToSession(mMediaBrowser?.sessionToken)
            } catch (e: RemoteException) {
                hidePlaybackControls()
            }
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    private val controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.apply {
                updateWithMeta(metadata)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (shouldShowControls()) {
                podcastSessionStateManager.lastPlaybackState = state
                showPlaybackControls()
                return
            }

            hidePlaybackControls()
        }
    }

    protected fun setUp() {
        if (mMediaBrowser == null) {
            mMediaBrowser = MediaBrowserCompat(applicationContext,
                    ComponentName(applicationContext, MusicService::class.java),
                    mConnectionCallbacks, null).apply { connect() }
        }
    }

//    // Local play
//    fun playMedia(episode: DownloadDao.DownloadEpisode) {
//        val item: MediaMetadataCompat = MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, episode.postId)
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, episode.filename)
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
//                .build()
//
//        val bItem = MediaBrowserCompat.MediaItem(item.description,
//                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
//
//        PodcastSource.setItem(item)
//
//        val currentPLayingTitle = podcastSessionStateManager.currentTitle
//        val isSameMedia = currentPLayingTitle == episode.title
//
//        onMediaItemSelected(bItem, isSameMedia)
//    }

    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token?) {
        val mediaController = MediaControllerCompat(
                this, token!!)

        MediaControllerCompat.setMediaController(this, mediaController)
        mediaController.registerCallback(controllerCallback)

        if (shouldShowControls()) {
            showPlaybackControls()
        } else {
            hidePlaybackControls()
        }
    }

    private fun onMediaItemSelected(item: MediaBrowserCompat.MediaItem, isSameMedia: Boolean) {
        if (!item.isPlayable) return

        val controller = MediaControllerCompat.getMediaController(this) ?: return

        val controls = controller.transportControls

        if (!isSameMedia) {
            controls.playFromMediaId(item.mediaId, null)
            return
        }

        val state = controller.playbackState.state

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            controls.pause()
            return
        }

        controls.play()
    }

    private fun updateWithMeta(metadata: MediaMetadataCompat) {
        podcastSessionStateManager.setMediaMetaData(metadata)
    }

    // Playbar stuffs
    private fun showPlaybackControls() {
        playerControls.visibility = View.VISIBLE
    }

    private fun hidePlaybackControls() {
        playerControls.visibility = View.GONE
    }

    private fun shouldShowControls(): Boolean {
        val mediaController = MediaControllerCompat.getMediaController(this)
        if (mediaController == null ||
                mediaController.metadata == null ||
                mediaController.playbackState == null) {
            return false
        }
        when (mediaController.playbackState.state) {
            PlaybackStateCompat.STATE_ERROR, PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED -> return false
            else -> return true
        }
    }

}