package com.koalatea.sedaily.feature.commentList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.commentList.epoxy.CommentsEpoxyController
import com.koalatea.sedaily.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.comments_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CommentsFragment : BaseFragment() {

    private val viewModel: CommentsViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.comments_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: CommentsFragmentArgs by navArgs()
        val entityId = safeArgs.entityId

        epoxyRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        val commentsEpoxyController = CommentsEpoxyController()
        epoxyRecyclerView.setController(commentsEpoxyController)

        viewModel.commentsLiveData.observe(this, Observer { comments ->
            commentsEpoxyController.setData(comments)
        })

        viewModel.fetchComments(entityId)
    }

}
