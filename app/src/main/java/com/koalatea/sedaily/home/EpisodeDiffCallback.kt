package com.koalatea.sedaily.home

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.koalatea.sedaily.models.Episode

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