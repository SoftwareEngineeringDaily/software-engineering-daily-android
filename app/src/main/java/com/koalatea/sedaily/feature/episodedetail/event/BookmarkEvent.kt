package com.koalatea.sedaily.feature.episodedetail.event

data class BookmarkEvent(
        val bookmarked: Boolean,
        val failed: Boolean = false)