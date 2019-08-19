package com.koalatea.sedaily.feature.commentList.epoxy

import android.widget.Button
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R

@EpoxyModelClass(layout = R.layout.view_holder_reply)
abstract class ReplyEpoxyModelWithHolder : BaseCommentEpoxyModelWithHolder<ReplyHolder>() {

    @EpoxyAttribute lateinit var upVoteClickListener: () -> Unit

    override fun bind(holder: ReplyHolder) {
        super.bind(holder)

        holder.upVoteButton.setOnClickListener { upVoteClickListener() }
        holder.upVoteButton.isSelected = upvoted
        holder.upVoteButton.text = score?.let {
            if(it > 0) { it.toString() } else {""}
        }
    }
}

class ReplyHolder : BaseCommentHolder() {
    val upVoteButton by bind<Button>(R.id.replyUpvoteButton)
}