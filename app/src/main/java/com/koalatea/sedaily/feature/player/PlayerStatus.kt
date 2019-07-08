package com.koalatea.sedaily.feature.player

sealed class PlayerStatus(open val episodeId: String?) {
    data class Other(override val episodeId: String? = null) : PlayerStatus(episodeId)
    data class Playing(override val episodeId: String) : PlayerStatus(episodeId)
    data class Paused(override val episodeId: String) : PlayerStatus(episodeId)
    data class Ended(override val episodeId: String) : PlayerStatus(episodeId)
    data class Error(override val episodeId: String, val exception: Exception?) : PlayerStatus(episodeId)
}