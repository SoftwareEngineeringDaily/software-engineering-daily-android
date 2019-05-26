package com.koalatea.sedaily.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Download(
        @field:PrimaryKey
        val postId: String,
        val filename: String
)