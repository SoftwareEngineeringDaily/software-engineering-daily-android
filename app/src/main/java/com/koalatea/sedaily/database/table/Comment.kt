package com.koalatea.sedaily.database.table

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(
        val _id: String,
        val author: Author,
        val rootEntity: String,
        val content: String,
        val mentions: List<String>?,
        val deleted: Boolean?,
        val dateCreated: String,
        val score: Int?,
        val replies: List<Comment>?,
        val upvoted: Boolean?,
        val downvoted: Boolean?
) : Parcelable