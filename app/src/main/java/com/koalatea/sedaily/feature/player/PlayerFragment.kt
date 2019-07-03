package com.koalatea.sedaily.feature.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.PlayerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.audio_controller_view.*
import kotlinx.android.synthetic.main.fragment_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

private const val ARG_EPISODE_ID = "episode_id"
private const val ARG_ONLY_SHOW_PLAYER = "auto_play"

private const val TAG_DIALOG_PLAYBACK_SPEED = "playback_speed_dialog"

class PlayerFragment : BaseFragment(), PlaybackSpeedDialogFragment.OnPlaybackChangedListener {

    companion object {
        fun newInstance(episodeId: String, isOnlyShowPlayer: Boolean): PlayerFragment {
            val fragment = PlayerFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_EPISODE_ID, episodeId)
                putBoolean(ARG_ONLY_SHOW_PLAYER, isOnlyShowPlayer)
            }

            return fragment
        }
    }
    
    private val viewModel: PlayerViewModel by viewModel()

    private var audioService: AudioService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            // Attach the ExoPlayer to the PlayerView.
            (view as? PlayerView)?.apply { player = audioService?.exoPlayer }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    private var isOnlyShowPlayer: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isOnlyShowPlayer = arguments?.getBoolean(ARG_ONLY_SHOW_PLAYER) ?: false

        playbackSpeedButton.setOnClickListener {
            PlaybackSpeedDialogFragment.show(this, requireFragmentManager(), TAG_DIALOG_PLAYBACK_SPEED)
        }

        viewModel.episodeDetailsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {} // Do nothing.
                is Resource.Success<EpisodeDetails> -> renderContent(resource.data)
                is Resource.Error -> acknowledgeGenericError()
            }
        })

        viewModel.playMediaLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { pair ->
                if (isOnlyShowPlayer) {
                    isOnlyShowPlayer = false
                } else {
                    val episode = pair.first
                    val playbackSpeed = pair.second

                    AudioService.newIntent(requireContext(), episode, playbackSpeed).also { intent ->
                        // This service will get converted to foreground service using the PlayerNotificationManager notification Id.
                        activity?.startService(intent)
                    }
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

        arguments?.getString(ARG_EPISODE_ID)?.also { episodeId ->
            viewModel.play(episodeId)
        }

        playerView.showController()
    }

    override fun onStart() {
        super.onStart()

        AudioService.newIntent(requireContext()).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        activity?.unbindService(connection)
        audioService = null

        super.onStop()
    }

    override fun onPlaybackSpeedChanged(playbackSpeed: Float) {
        viewModel.changePlaybackSpeed(playbackSpeed)
    }

    fun play(episode: Episode) = viewModel.play(episode._id)

    fun stop() {
        activity?.stopService(Intent(context, AudioService::class.java))
    }

    private fun renderContent(episodeDetails: EpisodeDetails) {
        playerView.findViewById<TextView>(R.id.titleTextView).text = episodeDetails.episode.titleString ?: getString(R.string.loading_dots)
    }

}