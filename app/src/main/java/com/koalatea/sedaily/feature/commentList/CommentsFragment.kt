package com.koalatea.sedaily.feature.commentList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.model.Comment
import com.koalatea.sedaily.feature.commentList.epoxy.CommentsEpoxyController
import com.koalatea.sedaily.feature.commentList.epoxy.CommentsItemDecoration
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.hideKeyboard
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.include_empty_state.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
//
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
                },
                upVoteClickListener = {comment ->
                    viewModel.upVoteComment(comment)
                }
        ).apply {
            epoxyRecyclerView.setController(this)

            // Scroll to the bottom when the data is loaded.
            this.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    epoxyRecyclerView.layoutManager?.scrollToPosition(commentsEpoxyController?.adapter?.itemCount?.minus(1) ?: 0)
                }
            })
        }



        addCommentButton.setOnClickListener {
            val comment = commentEditText.text?.trim()?.toString() ?: ""

            viewModel.addComment(comment)
        }

        cancelReplyButton.setOnClickListener {
            viewModel.cancelReply()
        }

        viewModel.commentVoteLiveData.observe(this, Observer { it.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Resource.RequireLogin -> showPromptLoginDialog()
                    is Resource.Success -> {
                        viewModel.reloadComments(entityId)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
                    }
                }
            }
        })

        viewModel.commentsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (commentsEpoxyController?.currentData.isNullOrEmpty()) {
                        showLoading()
                    }
                }
                is Resource.Success<List<Comment>> -> {
                    if (resource.data.isEmpty()) {
                        showNoCommentsEmptyState()
                    } else {
                        renderComments(resource.data)
                    }
                }
                is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
            }
        })

        viewModel.replyToCommentLiveData.observe(this, Observer { comment ->
            val replyViewsVisibility = if (comment != null) View.VISIBLE else View.GONE

            replyTextView.text = comment?.let {
                getString(R.string.reply_to_, comment.author.name ?: comment.author.username)
            } ?: getString(R.string.reply)
            replyTextView.visibility = replyViewsVisibility
            cancelReplyButton.visibility = replyViewsVisibility
        })

        viewModel.addCommentLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { resource ->
                addCommentButton.isEnabled = true

                when (resource) {
                    is Resource.RequireLogin -> showPromptLoginDialog()
                    is Resource.Loading -> addCommentButton.isEnabled = false
                    is Resource.Success -> {
                        if (resource.data) {
                            resetContent()
                            acknowledgeAddCommentSuccess()

                            // Reload comments
                            viewModel.reloadComments(entityId)
                        } else {
                            acknowledgeConnectionError()
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()

                        if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
                    }
                }
            }
        })

        viewModel.fetchComments(entityId)
    }

    private fun showLoading() {
        hideContent(hideAddComment = true)
        emptyStateContainer.visibility = View.GONE

        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showNoCommentsEmptyState() {
        hideContent(hideAddComment = false)
        progressBar.visibility = View.GONE

        emptyStateContainer.textView.text = getString(R.string.no_comments_yet)
        emptyStateContainer.visibility = View.VISIBLE
    }

    private fun showContent() {
        epoxyRecyclerView.visibility = View.VISIBLE
        addCommentSeparatorView.visibility = View.VISIBLE
        addCommentContainer.visibility = View.VISIBLE
    }

    private fun hideContent(hideAddComment: Boolean = false) {
        epoxyRecyclerView.visibility = View.GONE

        if (hideAddComment) {
            addCommentSeparatorView.visibility = View.GONE
            addCommentContainer.visibility = View.GONE
        } else {
            addCommentSeparatorView.visibility = View.VISIBLE
            addCommentContainer.visibility = View.VISIBLE
        }
    }

    private fun resetContent() {
        commentEditText.text = null

        activity?.hideKeyboard()
    }

    private fun renderComments(comments: List<Comment>) {
        commentsEpoxyController?.setData(comments.reversed())

        emptyStateContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        showContent()
    }

    private fun showPromptLoginDialog() {
        AlertDialogFragment.show(
                requireFragmentManager(),
                message = getString(R.string.prompt_login),
                positiveButton = getString(R.string.ok),
                tag = TAG_DIALOG_PROMPT_LOGIN)
    }

    private fun acknowledgeAddCommentSuccess()
            = Snackbar.make(requireView(), R.string.add_comment_success, Snackbar.LENGTH_SHORT).show()

}
