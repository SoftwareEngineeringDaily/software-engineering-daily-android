package com.koalatea.sedaily.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Episode(
        @field:PrimaryKey
        val _id: String,
        val mp3: String?,
        val title: Title?,
        val content: Content?,
        val excerpt: Excerpt?,
        val featuredImage: String?,
        val date: String?,
        val score: Int?,
        val upvoted: Boolean?,
        val bookmarked: Boolean?,
        val thread: Thread?
) {
    var searchQueryHashCode: Int? = null
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1
}