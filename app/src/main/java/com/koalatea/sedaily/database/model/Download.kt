package com.koalatea.sedaily.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Download(
        @field:PrimaryKey
        val postId: String,
        val downloadId: Long
)