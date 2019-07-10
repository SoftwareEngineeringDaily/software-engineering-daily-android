package com.koalatea.sedaily.feature.commentList.epoxy

import android.widget.Button
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.koalatea.sedaily.R

@EpoxyModelClass(layout = R.layout.view_holder_comment)
abstract class CommentEpoxyModelWithHolder : BaseCommentEpoxyModelWithHolder<CommentHolder>() {

    @EpoxyAttribute lateinit var replyClickListener: () -> Unit

    override fun bind(holder: CommentHolder) {
        super.bind(holder)

        holder.replayButton.setOnClickListener { replyClickListener() }
    }

}

class CommentHolder : BaseCommentHolder() {
    val replayButton by bind<Button>(R.id.replyButton)
}