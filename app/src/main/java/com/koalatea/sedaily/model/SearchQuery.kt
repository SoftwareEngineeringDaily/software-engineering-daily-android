package com.koalatea.sedaily.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchQuery(val searchTerm: String? = null, val categoryId: String? = null, val tagId: String? = null) : Parcelable
