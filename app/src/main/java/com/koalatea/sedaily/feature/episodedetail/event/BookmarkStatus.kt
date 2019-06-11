package com.koalatea.sedaily.feature.episodedetail.event

data class BookmarkStatus(
        val bookmarked: Boolean,
        val failed: Boolean = false)