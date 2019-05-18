package com.koalatea.sedaily

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.koalatea.sedaily.auth.UserRepository
import com.koalatea.sedaily.databinding.ActivityMainBinding
import com.koalatea.sedaily.home.PodcastSearchRepo

class MainActivity : PlaybackActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val SEDAILY_EXTERNAL_PERMISSION_REQUEST = 987;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_main)

        // Set set up bar
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE
        val bar = findViewById<BottomAppBar>(R.id.bar)
        setSupportActionBar(bar)

        // Set up media
        this.setUp()
        checkForPermissions()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        UserRepository.getInstance().getToken()?.apply {
            if (UserRepository.getInstance().getToken() != "") {
                setLogout(menu)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.search -> {
                onSearchRequested()
                true
            }
            R.id.home -> {
                Navigation
                        .findNavController(this, R.id.garden_nav_fragment)
                        .navigate(R.id.garden_fragment)
                true
            }
            R.id.downloads -> {
                Navigation
                        .findNavController(this, R.id.garden_nav_fragment)
                        .navigate(R.id.downloads_fragment)
                true
            }
            R.id.auth -> {
                Navigation
                        .findNavController(this, R.id.garden_nav_fragment)
                        .navigate(R.id.auth_fragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchPodcasts(query)
            }
        }
    }

    private fun searchPodcasts(query: String) {
        PodcastSearchRepo.getInstance().setSearch(query)
    }

    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        SEDAILY_EXTERNAL_PERMISSION_REQUEST)

//            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SEDAILY_EXTERNAL_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(drawerLayout,
                Navigation.findNavController(this, R.id.garden_nav_fragment))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE
    }

    fun setLogout (menu: Menu) {
        val authItem = menu.findItem(R.id.auth)
        authItem?.title = "Logout"
        authItem.setOnMenuItemClickListener {
            UserRepository.getInstance().setToken("")
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
            false
        }
    }
}
