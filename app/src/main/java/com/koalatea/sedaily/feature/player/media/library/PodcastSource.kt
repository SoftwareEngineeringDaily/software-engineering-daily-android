package com.koalatea.sedaily.feature.player.media.library

import android.support.v4.media.MediaMetadataCompat
import com.koalatea.sedaily.feature.player.media.extensions.id

class PodcastSource : AbstractMusicSource() {
    init {
        state = STATE_INITIALIZED
    }

    companion object {
        private var catalog: MutableList<MediaMetadataCompat> = emptyList<MediaMetadataCompat>().toMutableList()

        fun setItem(item: MediaMetadataCompat) {
            // @TOOD: Remove by id?
            val itemFound = catalog.find { catItem -> catItem.id === item.id }
            itemFound?.apply {
                val index = catalog.indexOf(this)
                catalog.removeAt(index)
            }
            catalog.add(item)
        }
    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()
}