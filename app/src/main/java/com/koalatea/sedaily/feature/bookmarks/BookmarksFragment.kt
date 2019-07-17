package com.koalatea.sedaily.feature.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.bookmarks.epoxy.BookmarksEpoxyController
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import kotlinx.android.synthetic.main.include_empty_state.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"

class BookmarksFragment : BaseFragment() {

    private val viewModel: BookmarksViewModel by viewModel()

    private var bookmarksEpoxyController: BookmarksEpoxyController? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_bookmarks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        epoxyRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        bookmarksEpoxyController = BookmarksEpoxyController(
                upvoteClickListener = { episode ->
                    viewModel.toggleUpvote(episode)
                },
                commentClickListener = { episode ->
                    episode.thread?._id?.let { threadId ->
                        val direction = BookmarksFragmentDirections.openCommentsAction(threadId)
                        findNavController().navigate(direction)
                    } ?: acknowledgeGenericError()
                },
                bookmarkClickListener = { episode ->
                    viewModel.toggleBookmark(episode)
                },
                episodeClickListener = { episode ->
                    val direction = BookmarksFragmentDirections.openEpisodeDetailsAction(episode)
                    findNavController().navigate(direction)
                }
        ).apply {
            epoxyRecyclerView.setController(this)
        }

        bookmarksSwipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchBookmarks()
        }

        viewModel.bookmarksResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (bookmarksEpoxyController?.currentData.isNullOrEmpty()) {
                        showLoading()
                    }
                }
                is Resource.RequireLogin -> showLoginEmptyState()
                is Resource.Success<List<Episode>> -> renderBookmarks(resource.data)
                is Resource.Error -> {
                    hideLoading()

                    if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
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

        viewModel.fetchBookmarks()
    }

    private fun showLoading() {
        emptyStateContainer.visibility = View.GONE
        bookmarksSwipeRefreshLayout.visibility = View.GONE

        bookmarksSwipeRefreshLayout.isRefreshing = true
    }

    private fun hideLoading() {
        bookmarksSwipeRefreshLayout.isRefreshing = false
    }

    private fun showLoginEmptyState() {
        bookmarksSwipeRefreshLayout.visibility = View.GONE
        bookmarksSwipeRefreshLayout.isRefreshing = false

        emptyStateContainer.textView.text = getString(R.string.login_to_manage_bookmarks)
        emptyStateContainer.visibility = View.VISIBLE
    }

    private fun renderBookmarks(episodes: List<Episode>) {
        bookmarksEpoxyController?.setData(episodes)

        emptyStateContainer.visibility = View.GONE
        bookmarksSwipeRefreshLayout.isRefreshing = false

        bookmarksSwipeRefreshLayout.visibility = View.VISIBLE
    }

}