package com.koalatea.sedaily.feature.episodedetail.event

data class UpvoteStatus(
        val upvoted: Boolean,
        val score: Int,
        val failed: Boolean = false)