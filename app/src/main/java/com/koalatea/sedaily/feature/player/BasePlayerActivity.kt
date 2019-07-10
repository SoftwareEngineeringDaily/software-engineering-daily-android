package com.koalatea.sedaily.feature.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.BaseWindowCallback
import com.koalatea.sedaily.util.isServiceRunning
import com.koalatea.sedaily.util.pointInView
import kotlinx.android.synthetic.main.audio_controller_view.*
import kotlinx.android.synthetic.main.include_bottom_sheet_player.*
import kotlinx.android.synthetic.main.include_content_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat

private const val TAG_DIALOG_PLAYBACK_SPEED = "playback_speed_dialog"

abstract class BasePlayerActivity : AppCompatActivity(), PlayerCallback, PlaybackSpeedDialogFragment.OnPlaybackChangedListener {

    private inner class PlayerWindowCallback(val originalCallback: Window.Callback) : BaseWindowCallback(originalCallback) {

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            // Collapse the bottom sheet if touch will not be handle by the bottom sheet view.
            if (event.action == MotionEvent.ACTION_UP &&
                    findViewById<View>(R.id.stopButton)?.pointInView(event.x, event.y) == false &&
                    !playerOverlayContainerConstraintLayout.pointInView(event.x, event.y)) {
                collapsePlayerOverlay()
            }

            return super.dispatchTouchEvent(event)
        }

    }

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

                playerOverlayPlayMaterialButton.isSelected = it is PlayerStatus.Playing
            })

            // Show player after config change.
            val episodeId = audioService?.episodeId
            if (episodeId != null) {
                showPlayerOverlay()

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

        setupPlayerBottomSheet()

        // Show the player, if the audio service is already running.
        if (applicationContext.isServiceRunning(AudioService::class.java.name)) {
            bindToAudioService()
        }

        playerOverlayPlayMaterialButton.setOnClickListener {
            if (playerOverlayPlayMaterialButton.isSelected) {
                audioService?.pause()
            } else {
                audioService?.resume()
            }
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
            val formatter = DecimalFormat("0.#")
            playbackSpeedButton.text = getString(R.string.playback_speed, formatter.format(it.peekContent()))

            it.getContentIfNotHandled()?.let { playbackSpeed ->
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
        showPlayerOverlay()

        bindToAudioService()

        viewModel.play(episode._id)
    }

    override fun stop() {
        dismissPlayerOverlay()

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

    private fun setupPlayerBottomSheet() {
        dismissPlayerOverlay()

        playerOverlayPeekLinearLayout.setOnClickListener {
            togglePlayerOverlayShowState()
        }

        BottomSheetBehavior.from(playerOverlayContainerConstraintLayout).apply {
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> hidePlayerOverlayPlaceHolder()
                    BottomSheetBehavior.STATE_EXPANDED -> showPlayerOverlayPlaceHolder()
                    BottomSheetBehavior.STATE_COLLAPSED -> showPlayerOverlayPlaceHolder()
                    else -> { }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    private fun renderContent(episodeDetails: EpisodeDetails) {
        val episodeTitle = episodeDetails.episode.titleString ?: getString(R.string.loading_dots)

        playerOverlayTitleTextView.text = episodeTitle
        playerView.findViewById<TextView>(R.id.titleTextView).text = episodeTitle

        Glide.with(this)
                .load(episodeDetails.episode.httpsGuestImageUrl)
                .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                .placeholder(R.drawable.vd_image)
                .error(R.drawable.vd_broken_image)
                .into(playerOverlayImageView)
    }

    private fun showPlayerOverlay() {
        // Monitor outside touches when player overlay is collapsed or expanded only.
        window.callback = PlayerWindowCallback(window.callback)

        BottomSheetBehavior.from(playerOverlayContainerConstraintLayout).apply {
            isHideable = false

            showPlayerOverlayPlaceHolder()
        }
    }

    private fun collapsePlayerOverlay() {
        BottomSheetBehavior.from(playerOverlayContainerConstraintLayout).apply {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun togglePlayerOverlayShowState() {
        BottomSheetBehavior.from(playerOverlayContainerConstraintLayout).apply {
            val collapse = state == BottomSheetBehavior.STATE_EXPANDED

            state = if (collapse) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun dismissPlayerOverlay() {
        // Restore original window callback.
        (window.callback as? PlayerWindowCallback)?.originalCallback?.let{
            window.callback = it
        }

        BottomSheetBehavior.from(playerOverlayContainerConstraintLayout).apply {
            isHideable = true

            hidePlayerOverlayPlaceHolder()
            state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showPlayerOverlayPlaceHolder() {
        collapsedPlayerOverlayPlaceHolderView.visibility = View.VISIBLE
    }

    private fun hidePlayerOverlayPlaceHolder() {
        collapsedPlayerOverlayPlaceHolderView.visibility = View.GONE
    }

}