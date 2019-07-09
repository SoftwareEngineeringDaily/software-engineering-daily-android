package com.koalatea.sedaily

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.koalatea.sedaily.feature.episodes.EpisodesFragmentArgs
import com.koalatea.sedaily.feature.player.BasePlayerActivity
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.util.dpToPx
import com.koalatea.sedaily.util.hideKeyboard
import com.koalatea.sedaily.util.setupActionBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_content_main.*
import kotlinx.android.synthetic.main.include_default_toolbar.*

class MainActivity : BasePlayerActivity() {

    private val onGlobalLayoutListener = OnGlobalLayoutListener {
        val heightDiff = rootContainer.rootView.height - rootContainer.height
        if (heightDiff > this@MainActivity.dpToPx(200f)) { // if more than 200 dp, it's probably a keyboard...
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = mainNavHostFragment.findNavController()
        // Define top level screens, these will not have a back action.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_bookmarks, R.id.navigation_profile))

        navController.setupActionBar(this, appBarConfiguration)
        setupBottomNavMenu(navController)

        rootContainer.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { handleIntent(intent) }
    }

    override fun onDestroy() {
        super.onDestroy()

        rootContainer.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            hideKeyboard()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupBottomNavMenu(navController: NavController) = bottomNavigationView?.setupWithNavController(navController)

    override fun onSupportNavigateUp(): Boolean {
        return mainNavHostFragment.findNavController().navigateUp()
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchEpisodes(query)
            }
        }
    }

    private fun searchEpisodes(query: String) {
        supportActionBar?.title = query

        mainNavHostFragment
                .findNavController()
                .navigate(R.id.navigation_episodes,
                        EpisodesFragmentArgs.Builder(SearchQuery(searchTerm = query), true, true).build().toBundle())
    }

}
