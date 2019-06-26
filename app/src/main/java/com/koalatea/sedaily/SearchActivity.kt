package com.koalatea.sedaily

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.koalatea.sedaily.feature.episodes.EpisodesFragment
import com.koalatea.sedaily.feature.episodes.EpisodesFragmentArgs
import com.koalatea.sedaily.model.SearchQuery
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar)

//        setupActionBarWithNavController(mainNavHostFragment.findNavController())

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { handleIntent(intent) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        return mainNavHostFragment.findNavController().navigateUp()
//    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchEpisodes(query)
            }
        }
    }

    private fun searchEpisodes(query: String) {
        supportActionBar?.title = query

        navHostFragment
                .findNavController()
                .navigate(R.id.navigation_episodes,
                        EpisodesFragmentArgs.Builder(SearchQuery(searchTerm = query), true, true).build().toBundle())
    }

}