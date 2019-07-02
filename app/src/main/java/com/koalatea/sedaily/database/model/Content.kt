package com.koalatea.sedaily.database.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Content(
        val rendered: String
) : Parcelable