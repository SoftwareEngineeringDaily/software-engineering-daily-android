package com.koalatea.sedaily.database.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Author (
	val _id : String,
	val email : String?,
	val username : String?,
	val avatarUrl : String?,
	val name : String,
	val website : String?,
	val bio : String?,
	val createdAt : String?,
	val signedupForNewsletter : Boolean?,
	val topics : List<String>?,
	val isAdmin : Boolean?,
	val verified : Boolean?
) : Parcelable