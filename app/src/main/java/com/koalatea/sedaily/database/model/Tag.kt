package com.koalatea.sedaily.database.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tag(
        val id: Long,
        val slug: String,
        val name: String
) : Parcelable