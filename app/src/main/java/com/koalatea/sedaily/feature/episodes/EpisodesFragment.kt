package com.koalatea.sedaily.feature.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.episodes.epoxy.EpisodesEpoxyController
import com.koalatea.sedaily.feature.home.HomeFragmentDirections
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_episodes.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodesFragment : Fragment() {

    companion object {
        fun newInstance(categoryId: String?): EpisodesFragment {
            val fragment = EpisodesFragment()
            fragment.arguments = EpisodesFragmentArgs.Builder(SearchQuery(categoryId = categoryId), false).build().toBundle()

            return fragment
        }
    }

    private val viewModel: EpisodesViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_episodes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: EpisodesFragmentArgs by navArgs()
        val searchQuery = safeArgs.searchQuery ?: SearchQuery()
        viewModel.doNotCache = safeArgs.doNotCache

        supportActionBar?.elevation = 0f

        epoxyRecyclerView.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        val episodesEpoxyController = EpisodesEpoxyController(
                upvoteClickListener = { episode ->
                    viewModel.toggleUpvote(episode)
                },
                commentClickListener = { episode ->
                    episode.thread?._id?.let { threadId ->
                        val direction = HomeFragmentDirections.openCommentsAction(threadId)
                        findNavController().navigate(direction)
                    } ?: acknowledgeGenericError()
                },
                bookmarkClickListener = { episode ->
                    viewModel.toggleBookmark(episode)
                },
                episodeClickListener = { episode ->
                    val direction = HomeFragmentDirections.openEpisodeDetailsAction(episode._id)
                    findNavController().navigate(direction)
                }
        )
        epoxyRecyclerView.setControllerAndBuildModels(episodesEpoxyController)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.episodesPagedList.observe(this, Observer { results ->
//            showEmptyList(it?.size == 0)
            episodesEpoxyController.submitList(results)
            epoxyRecyclerView.requestModelBuild()
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                is NetworkState.Error -> { acknowledgeError(it.message) }
                else -> { }// Ignore
            }
        })

        viewModel.refreshState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it == NetworkState.Loading

            when (it) {
                is NetworkState.Loaded -> {
                    if (it.itemsCount == 0) {
                        // TODO :: Handle empty state
                    }
                }
                else -> { }// Ignore
            }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
//                val direction = HomeFragmentDirections.openCommentsAction(episodeId)
//                findNavController().navigate(direction)
                Toast.makeText(context, "Debug :: Login first", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.fetchPosts(searchQuery)
    }

    private fun acknowledgeGenericError() = Snackbar.make(swipeRefreshLayout, R.string.error_generic, Snackbar.LENGTH_SHORT).show()
    private fun acknowledgeError(message: String) = Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show()
}
