package com.koalatea.sedaily.feature.player

import com.koalatea.sedaily.database.model.Episode

interface PlayerCallback {

    fun play(episode: Episode)
    fun stop()

}