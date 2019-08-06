package com.koalatea.sedaily.feature.commentList.epoxy

import android.graphics.Typeface
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R

@EpoxyModelClass(layout = R.layout.view_holder_reply)
abstract class ReplyEpoxyModelWithHolder : BaseCommentEpoxyModelWithHolder<ReplyHolder>() {

    @EpoxyAttribute lateinit var upVoteClickListener: () -> Unit
    @EpoxyAttribute var textWeight: Int = Typeface.NORMAL

    override fun bind(holder: ReplyHolder) {
        super.bind(holder)

        holder.upVoteButton.setOnClickListener { upVoteClickListener() }
        holder.upVoteButton.setTypeface(null, textWeight)
    }
}

class ReplyHolder : BaseCommentHolder() {
    val upVoteButton by bind<Button>(R.id.replyUpvoteButton)
}