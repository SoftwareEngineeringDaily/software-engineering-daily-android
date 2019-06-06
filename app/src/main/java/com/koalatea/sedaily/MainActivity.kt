package com.koalatea.sedaily

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.koalatea.sedaily.feature.auth.UserRepository
import com.koalatea.sedaily.util.setupActionBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_default_toolbar.*
import org.koin.android.ext.android.inject

class MainActivity : PlaybackActivity() {

    private val userRepository: UserRepository by inject()

//    private val SEDAILY_EXTERNAL_PERMISSION_REQUEST = 987

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = mainNavHostFragment.findNavController()
        // Define top level screens.
        // FIXME :: Use correct top-level tabs, setOf(R.id.navigation_home, R.id.navigation_saved, R.id.navigation_profile)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_downloads, R.id.navigation_auth))

        navController.setupActionBar(this, appBarConfiguration)
        setupBottomNavMenu(navController)

        // Set up media
        this.setUp()
//        checkForPermissions()
        handleIntent(intent)
    }

    private fun setupBottomNavMenu(navController: NavController) = bottomNavigationView?.setupWithNavController(navController)

//    override fun onNewIntent(intent: Intent) {
//        setIntent(intent)
//        handleIntent(intent)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        userRepository.token?.let { token ->
            if (token.isNotBlank()) {
                setLogout(menu)
            }
        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                onSearchRequested()
                true
            }
            R.id.home -> {
                mainNavHostFragment.findNavController().navigate(R.id.navigation_home)
                true
            }
            else -> item.onNavDestinationSelected(mainNavHostFragment.findNavController()) || super.onOptionsItemSelected(item)
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

    }

//    private fun checkForPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
////            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
////                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
////            } else {
//            ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    SEDAILY_EXTERNAL_PERMISSION_REQUEST)
//
////            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            SEDAILY_EXTERNAL_PERMISSION_REQUEST -> {
//                // If request is cancelled, the result arrays are empty.
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return
//            }
//
//            // Add other 'when' lines to check for other
//            // permissions this app might request.
//            else -> {
//                // Ignore all other requests.
//            }
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        return mainNavHostFragment.findNavController().navigateUp()
    }

    fun setLogout(menu: Menu) {
        val authItem = menu.findItem(R.id.navigation_auth)
        authItem?.title = "Logout"
        authItem.setOnMenuItemClickListener {
            userRepository.token = ""
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
            false
        }
    }
}
