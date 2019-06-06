package com.koalatea.sedaily.database.table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Download(
        @field:PrimaryKey
        val postId: String,
        val downloadId: Long
)