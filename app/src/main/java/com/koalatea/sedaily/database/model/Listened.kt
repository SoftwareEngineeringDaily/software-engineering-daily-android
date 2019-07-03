package com.koalatea.sedaily.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Listened(
        @field:PrimaryKey
        val postId: String,
        val startPosition: Long,
        val total: Long
) : Parcelable