package com.koalatea.sedaily.model

data class RelatedLink(
        val _id: String,
        val author: String,
        val post: String,
        val title: String,
        val url: String,
        val deleted: Boolean?,
        val dateCreated: String,
        val score: Int?,
        val clicks: Int?,
        val image: String?,
        val upvoted: Boolean?,
        val downvoted: Boolean?
)