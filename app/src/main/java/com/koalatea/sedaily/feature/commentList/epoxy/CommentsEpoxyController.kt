package com.koalatea.sedaily.feature.commentList.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import com.koalatea.sedaily.database.model.Comment

class CommentsEpoxyController : TypedEpoxyController<List<Comment>>() {

    override fun buildModels(comments: List<Comment>) {
        comments.forEach { comment ->
            val imageHttpsUrl = comment.author.avatarUrl?.replace(Regex("^http://"), "https://")

            commentEpoxyModelWithHolder {
                id(comment._id)
                authorImageUrl(imageHttpsUrl)
                authorName(comment.author.name)
                comment(comment.content)
                date(comment.dateCreated)
            }
        }
    }

    // FIXME :: Remove
    init {
        isDebugLoggingEnabled = true
    }

    // FIXME :: Timber log
    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}