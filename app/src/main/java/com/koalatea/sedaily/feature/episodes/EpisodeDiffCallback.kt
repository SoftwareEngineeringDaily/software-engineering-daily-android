package com.koalatea.sedaily.feature.episodes

import androidx.recyclerview.widget.DiffUtil
import com.koalatea.sedaily.model.Episode

class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(
            oldItem: Episode,
            newItem: Episode
    ): Boolean {
        return oldItem.title?.rendered == newItem.title?.rendered
    }

    override fun areContentsTheSame(
            oldItem: Episode,
            newItem: Episode
    ): Boolean {
        return oldItem == newItem
    }
}