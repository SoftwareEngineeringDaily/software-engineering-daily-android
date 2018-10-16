package com.koalatea.sedaily.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Episode (
    @field:PrimaryKey
    val _id: String,
    val mp3: String?,
    val title: Title?,
    val content: Content?,
    val featuredImage: String?,
    val date: String?
)