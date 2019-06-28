package com.koalatea.sedaily.feature.commentList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Comment
import com.koalatea.sedaily.feature.commentList.epoxy.CommentsEpoxyController
import com.koalatea.sedaily.feature.commentList.epoxy.CommentsItemDecoration
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.include_empty_state.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"

class CommentsFragment : BaseFragment() {

    private val viewModel: CommentsViewModel by viewModel()

    private var commentsEpoxyController: CommentsEpoxyController? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_comments, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: CommentsFragmentArgs by navArgs()
        val entityId = safeArgs.entityId

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        supportActionBar?.title = getString(R.string.comments_title)

        epoxyRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val commentDividerLeft = resources.getDimension(R.dimen.comment_divider_left)
        epoxyRecyclerView.addItemDecoration(CommentsItemDecoration(requireContext(), commentDividerLeft.toInt()))
        commentsEpoxyController = CommentsEpoxyController(
                replyClickListener = { comment ->
                    viewModel.replyTo(comment)
                }
        ).apply {
            epoxyRecyclerView.setController(this)
        }

        addCommentButton.setOnClickListener {
            val comment = commentEditText.text?.trim()?.toString() ?: ""

            viewModel.addComment(comment)
        }

        cancelReplyButton.setOnClickListener {
            viewModel.cancelReply()
        }

        viewModel.commentsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (commentsEpoxyController?.currentData.isNullOrEmpty()) {
                        showLoading()
                    }
                }
                is Resource.RequireLogin -> showNoCommentsEmptyState()
                is Resource.Success<List<Comment>> -> renderComments(resource.data)
                is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
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

        viewModel.fetchComments(entityId)
    }

    private fun showLoading() {
        emptyStateContainer.visibility = View.GONE
        epoxyRecyclerView.visibility = View.GONE

        progressBar.visibility = View.VISIBLE
    }

    private fun showNoCommentsEmptyState() {
        epoxyRecyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE

        emptyStateContainer.textView.text = getString(R.string.no_comments_yet)
        emptyStateContainer.visibility = View.VISIBLE
    }

    private fun renderComments(comments: List<Comment>) {
        commentsEpoxyController?.setData(comments)

        emptyStateContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        epoxyRecyclerView.visibility = View.VISIBLE
    }

}
