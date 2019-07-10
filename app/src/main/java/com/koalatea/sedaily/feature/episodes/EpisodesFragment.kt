package com.koalatea.sedaily.feature.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.episodes.epoxy.EpisodesEpoxyController
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_episodes.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"

private const val KEY_SCROLL_POSITION = "scroll_position"

class EpisodesFragment : BaseFragment() {

    companion object {

        fun newInstance(searchQuery: SearchQuery, elevateToolbar: Boolean = false, doNotCache: Boolean = false): EpisodesFragment {
            val fragment = EpisodesFragment()
            fragment.arguments = EpisodesFragmentArgs.Builder(searchQuery, elevateToolbar, doNotCache).build().toBundle()

            return fragment
        }
    }

    private val viewModel: EpisodesViewModel by viewModel()

    private lateinit var episodesEpoxyController: EpisodesEpoxyController

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_episodes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: EpisodesFragmentArgs by navArgs()
        val searchQuery = safeArgs.searchQuery ?: SearchQuery()
        viewModel.doNotCache = safeArgs.doNotCache

        searchQuery.searchTerm?.let{ supportActionBar?.title = it }
        supportActionBar?.elevation = if (safeArgs.elevateToolbar) resources.getDimension(R.dimen.toolbar_elevation) else 0f

        epoxyRecyclerView.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        if (!::episodesEpoxyController.isInitialized) {
            episodesEpoxyController = EpisodesEpoxyController(
                    upvoteClickListener = { episode ->
                        viewModel.toggleUpvote(episode)
                    },
                    commentClickListener = { episode ->
                        episode.thread?._id?.let { threadId ->
                            val direction = EpisodesFragmentDirections.openCommentsAction(threadId)
                            findNavController().navigate(direction)
                        } ?: acknowledgeGenericError()
                    },
                    bookmarkClickListener = { episode ->
                        viewModel.toggleBookmark(episode)
                    },
                    episodeClickListener = { episode ->
                        val direction = EpisodesFragmentDirections.openEpisodeDetailsAction(episode)
                        findNavController().navigate(direction)
                    }
            )
        }

        epoxyRecyclerView.setControllerAndBuildModels(episodesEpoxyController)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.episodesPagedList.observe(this, Observer { results ->
            episodesEpoxyController.submitList(results)
            epoxyRecyclerView.requestModelBuild()
        })

        viewModel.networkState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it.peekContent() == NetworkState.Loading

            it.getContentIfNotHandled()?.let { networkState ->
                when (networkState) {
                    is NetworkState.Error -> {
                        if (!networkState.isConnected) {
                            acknowledgeConnectionError()
                        } else {
                            networkState.message?.let { acknowledgeError(it) } ?: acknowledgeGenericError()
                        }
                    }
                    else -> { }// Ignore
                }
            }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                AlertDialogFragment.show(
                        requireFragmentManager(),
                        message = getString(R.string.prompt_login),
                        positiveButton = getString(R.string.ok),
                        tag = TAG_DIALOG_PROMPT_LOGIN)
            }
        })

        viewModel.fetchPosts(searchQuery)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            epoxyRecyclerView.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        val scrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION)

                        epoxyRecyclerView.layoutManager?.scrollToPosition(scrollPosition)
                    }
                }
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val scrollPosition = (epoxyRecyclerView?.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() ?: 0
        outState.putInt(KEY_SCROLL_POSITION, scrollPosition)

        super.onSaveInstanceState(outState)
    }

}
