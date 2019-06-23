package com.koalatea.sedaily.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Profile (
	val _id : String,
	val email : String,
	val username : String,
	val password : String,
	val createdAt : String,
	val signedupForNewsletter : Boolean?,
	val topics : List<String>?,
	val isAdmin : Boolean?,
	val verified : Boolean?
) : Parcelable