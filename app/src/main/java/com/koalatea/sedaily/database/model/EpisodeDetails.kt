package com.koalatea.sedaily.database.model

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EpisodeDetails(
        @Embedded val episode: Episode,
        @Embedded val listened: Listened? = null
) : Parcelable