package com.koalatea.sedaily

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private val userRepository: UserRepository by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = mainNavHostFragment.findNavController()
        // Define top level screens.
        // FIXME :: Use correct top-level tabs, setOf(R.id.navigation_home, R.id.navigation_saved, R.id.navigation_profile)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_auth, R.id.navigation_auth))

        navController.setupActionBar(this, appBarConfiguration)
        setupBottomNavMenu(navController)

        handleIntent(intent)
    }

    private fun setupBottomNavMenu(navController: NavController) = bottomNavigationView?.setupWithNavController(navController)

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

    override fun onSupportNavigateUp(): Boolean {
        return mainNavHostFragment.findNavController().navigateUp()
    }

    private fun setLogout(menu: Menu) {
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
