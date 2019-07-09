package com.koalatea.sedaily.feature.episodedetail.event

data class UpvoteEvent(
        val upvoted: Boolean,
        val score: Int,
        val failed: Boolean = false)