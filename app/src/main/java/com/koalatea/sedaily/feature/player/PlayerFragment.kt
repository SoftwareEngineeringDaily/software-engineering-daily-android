package com.koalatea.sedaily.feature.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_URI = "uri"
private const val ARG_START_POSITION = "start_position"

class PlayerFragment : Fragment(), PlayerCallback {

    companion object {
        fun newInstance(episode: Episode): PlayerFragment {
            val fragment = PlayerFragment()
            fragment.arguments = Bundle().apply {
                // FIXME :: Use episode local ot remote uri
                // FIXME :: what if the url was null or empty
                putParcelable(ARG_URI, Uri.parse(episode.httpsMp3Url))
//                putLong(ARG_START_POSITION, startPosition)
            }

            return fragment
        }
    }
    
//    private val podcastSessionStateManager: PodcastSessionStateManager by inject()

    private val playerViewModel: PlayerViewModel by viewModel()

    private var audioService: AudioService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            // FIXME ::
//            ((view as? ViewGroup)?.getChildAt(0) as? PlayerView)?.player = audioService?.exoPlayer
            val playerView = view as? PlayerView
//            playerView?.controllerShowTimeoutMs = 0
//            playerView?.controllerHideOnTouch = false
            playerView?.player = audioService?.exoPlayer
//            view?.findViewById<PlayerView>(R.id.playerView)?.player = audioService?.exoPlayer
//            playerView.player = audioService?.exoPlayer

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

//        stop_player_btn.setOnClickListener {
//            startService(Intent(this, AudioService::class.java).apply {
//                putExtra(AudioService.PLAY_PAUSE_ACTION, 0)
//            })
//        }
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(context, AudioService::class.java).apply {
            putExtra(ARG_URI, arguments?.getParcelable<Uri>(ARG_URI))

            // FIXME :: RESET after first use
//            arguments?.remove(ARG_URI)
        }
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        activity?.unbindService(connection)
        audioService = null
    }

    override fun play(episode: Episode) {
        // FIXME :: Use episode local ot remote uri
        val uri = Uri.parse("https://traffic.libsyn.com/sedaily/2019_06_10_KubernetesStoragewithSaadAli.mp3")
        val startPosition = 0L

        audioService?.playMedia(uri, startPosition) // TODO :: ?: showError()
    }

    override fun stop() {
        activity?.stopService(Intent(context, AudioService::class.java))
    }

}