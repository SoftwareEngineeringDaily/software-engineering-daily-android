package com.koalatea.sedaily.feature.commentList.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import com.koalatea.sedaily.database.model.Comment

class CommentsEpoxyController : TypedEpoxyController<List<Comment>>() {

    override fun buildModels(comments: List<Comment>) {
        comments.forEach { comment ->
            commentEpoxyModelWithHolder {
                val imageHttpsUrl = comment.author.avatarUrl?.replace(Regex("^http://"), "https://")

                id(comment._id)
                authorImageUrl(imageHttpsUrl)
                authorName(comment.author.name)
                comment(comment.content)
                date(comment.utcDateCreated)
            }

            comment.replies?.forEach { reply ->
                replyEpoxyModelWithHolder {
                    val imageHttpsUrl = reply.author.avatarUrl?.replace(Regex("^http://"), "https://")

                    id(reply._id)
                    authorImageUrl(imageHttpsUrl)
                    authorName(reply.author.name)
                    comment(reply.content)
                    date(reply.utcDateCreated)
                }
            }
        }
    }

}