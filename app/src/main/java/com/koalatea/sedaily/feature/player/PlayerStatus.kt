package com.koalatea.sedaily.feature.player

import java.lang.Exception

sealed class PlayerStatus {
    object Other : PlayerStatus()
    data class Playing(val episodeId: String) : PlayerStatus()
    data class Paused(val episodeId: String) : PlayerStatus()
    data class Error(val episodeId: String, val exception: Exception?) : PlayerStatus()
}