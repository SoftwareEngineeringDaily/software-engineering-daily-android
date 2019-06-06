package com.koalatea.sedaily.database.table

data class Thread (
		val _id : String,
		val title : String?,
		val content : String?,
		val author : String?,
		val podcastEpisode : String?,
		val dateCreated : String?,
		val dateLastAcitiy : String?,
		val commentsCount : Int?,
		val deleted : Boolean?,
		val score : Int
)