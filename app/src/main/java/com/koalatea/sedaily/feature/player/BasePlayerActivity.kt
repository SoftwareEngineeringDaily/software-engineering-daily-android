package com.koalatea.sedaily.feature.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.util.isServiceRunning
import kotlinx.android.synthetic.main.audio_controller_view.*
import kotlinx.android.synthetic.main.bottom_sheet_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat

private const val TAG_DIALOG_PLAYBACK_SPEED = "playback_speed_dialog"

abstract class BasePlayerActivity : AppCompatActivity(), PlayerCallback, PlaybackSpeedDialogFragment.OnPlaybackChangedListener {

    private var audioService: AudioService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            // Attach the ExoPlayer to the PlayerView.
            playerView.player = binder.exoPlayer

            // Pass player updates to interested observers.
            audioService?.playerStatusLiveData?.observe(this@BasePlayerActivity, Observer {
                _playerStatusLiveData.value = it
            })

            // Show player after config change.
            val episodeId = audioService?.episodeId
            if (episodeId != null) {
                playerContainerConstraintLayout.visibility = View.VISIBLE

                viewModel.refreshIfNecessary(episodeId)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    private val _playerStatusLiveData: MutableLiveData<PlayerStatus> = MutableLiveData()
    override val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData

    private val viewModel: PlayerViewModel by viewModel()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Show the player, if the audio service is already running.
        if (applicationContext.isServiceRunning(AudioService::class.java.name)) {
            bindToAudioService()
        }

        playbackSpeedButton.setOnClickListener {
            PlaybackSpeedDialogFragment.show(supportFragmentManager, TAG_DIALOG_PLAYBACK_SPEED)
        }

        viewModel.episodeDetailsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> { } // Do nothing.
                is Resource.Success<EpisodeDetails> -> renderContent(resource.data)
                is Resource.Error -> { Timber.w(resource.exception) } // Episode details were not retrieved correctly! No need to notify the user.
            }
        })

        viewModel.playMediaLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { episode ->
                AudioService.newIntent(this, episode).also { intent ->
                    // This service will get converted to foreground service using the PlayerNotificationManager notification Id.
                    startService(intent)
                }
            }
        })

        viewModel.playbackSpeedLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { playbackSpeed ->
                val formatter = DecimalFormat("0.#")
                playbackSpeedButton.text = getString(R.string.playback_speed, formatter.format(playbackSpeed))

                audioService?.changePlaybackSpeed(playbackSpeed)
            }
        })

        playerView.showController()
    }

    override fun onStop() {
        unbindAudioService()

        super.onStop()
    }

    override fun onPlaybackSpeedChanged(playbackSpeed: Float) {
        viewModel.changePlaybackSpeed(playbackSpeed)
    }

    override fun play(episode: Episode) {
        playerContainerConstraintLayout.visibility = View.VISIBLE

        bindToAudioService()

        viewModel.play(episode._id)
    }

    override fun stop() {
        playerContainerConstraintLayout.visibility = View.GONE

        stopAudioService()
        _playerStatusLiveData.value = PlayerStatus.Other()
    }

    private fun bindToAudioService() {
        if (audioService == null) {
            AudioService.newIntent(this).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun unbindAudioService() {
        if (audioService != null) {
            unbindService(connection)

            audioService = null
        }
    }

    private fun stopAudioService() {
        audioService?.pause()

        unbindAudioService()
        stopService(Intent(this, AudioService::class.java))

        audioService = null
    }

    private fun renderContent(episodeDetails: EpisodeDetails) {
        playerView.findViewById<TextView>(R.id.titleTextView).text = episodeDetails.episode.titleString ?: getString(R.string.loading_dots)
    }

}