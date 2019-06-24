package com.koalatea.sedaily

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.player.AudioService
import com.koalatea.sedaily.feature.player.PlayerCallback
import com.koalatea.sedaily.feature.player.PlayerFragment
import com.koalatea.sedaily.feature.player.PlayerStatus
import com.koalatea.sedaily.util.isServiceRunning
import com.koalatea.sedaily.util.setupActionBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_default_toolbar.*

private const val TAG_FRAGMENT_PLAYER = "player_fragment"

class MainActivity : AppCompatActivity(), PlayerCallback {

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            val audioService = binder.service

            audioService.playerStatusLiveData.observe(this@MainActivity, Observer {
                _playerStatusLiveData.value = it
            })

            audioService.episodeId?.let { episodeId ->
                addPlayerFragment(episodeId, true)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private var playerFragment: PlayerFragment? = null
    private var isAudioServiceBound: Boolean = false

    private val _playerStatusLiveData: MutableLiveData<PlayerStatus> = MutableLiveData()
    override val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = mainNavHostFragment.findNavController()
        // Define top level screens, these will not have a back action.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_bookmarks, R.id.navigation_profile))

        navController.setupActionBar(this, appBarConfiguration)
        setupBottomNavMenu(navController)

        handleIntent(intent)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // If audio service is running, add the player fragment
        if (applicationContext.isServiceRunning(AudioService::class.java.name)) {
            AudioService.newIntent(this).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)

                isAudioServiceBound = true
            }
        }
    }

    override fun onStop() {
        unbindAudioService()

        super.onStop()
    }

    private fun setupBottomNavMenu(navController: NavController) = bottomNavigationView?.setupWithNavController(navController)

    override fun play(episode: Episode) {
        playerFragment?.play(episode) ?: addPlayerFragment(episode._id, false)
    }

    override fun stop() {
        unbindAudioService()
        _playerStatusLiveData.value = PlayerStatus.Other()

        playerFragment?.let {
            playerFragment?.stop()

            supportFragmentManager.beginTransaction().remove(it).commit()
            playerFragment = null
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchEpisodes(query)
            }
        }
    }

    private fun addPlayerFragment(episodeId: String, isOnlyShowPlayer: Boolean) {
        playerFragment = PlayerFragment.newInstance(episodeId, isOnlyShowPlayer).also {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, it, TAG_FRAGMENT_PLAYER).commit()
        }
    }

    private fun searchEpisodes(query: String) {
        // FIXME :: Search for real!
        Toast.makeText(this, "Search for $query", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return mainNavHostFragment.findNavController().navigateUp()
    }

    private fun unbindAudioService() {
        if (isAudioServiceBound) {
            unbindService(connection)

            isAudioServiceBound = false
        }
    }

}
