package com.koalatea.sedaily.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Listened(
        @field:PrimaryKey
        val postId: String,
        val startPosition: Long,
        val total: Long
)