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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.network.Resource
import kotlinx.android.synthetic.main.fragment_episode_detail.*
import kotlinx.android.synthetic.main.fragment_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_EPISODE_ID = "episode_id"

class PlayerFragment : Fragment(), PlayerCallback {

    companion object {
        fun newInstance(episodeId: String): PlayerFragment {
            val fragment = PlayerFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_EPISODE_ID, episodeId)
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
            // FIXME :: Release the notification
            // playerNotificationManager.setPlayer(null)

            audioService = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_player, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.episodeDetailsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {} // Do nothing.
                is Resource.Success<Episode> -> renderContent(resource.data)
                is Resource.Error -> acknowledgeGenericError()
            }
        })

        viewModel.playMediaLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { episode ->
                context?.let { context ->
                    AudioService.newIntent(context, episode.titleString, episode.uri, episode.startPosition).also { intent ->
                        // This service will get converted to foreground service using the PlayerNotificationManager notification Id.
                        activity?.startService(intent)
                    }
                } ?: acknowledgeGenericError()
            }
        })

        arguments?.getString(ARG_EPISODE_ID)?.also {
            viewModel.play(it)
        }
    }

    override fun onStart() {
        super.onStart()

        AudioService.newIntent(requireContext()).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        activity?.unbindService(connection)
        audioService = null
    }

    override fun play(episode: Episode) = viewModel.play(episode._id)

    override fun stop() {
        activity?.stopService(Intent(context, AudioService::class.java))
    }

    private fun renderContent(episode: Episode) {
        playerView.findViewById<TextView>(R.id.titleTextView).text = episode.titleString
    }

    private fun acknowledgeGenericError() = Snackbar.make(containerConstraintLayout, R.string.error_generic, Snackbar.LENGTH_SHORT).show()

}