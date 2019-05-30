package com.koalatea.sedaily.feature.episodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.home.HomeFragmentDirections
import com.koalatea.sedaily.model.Episode
import kotlinx.android.synthetic.main.view_holder_episode.view.*
import java.text.SimpleDateFormat

class EpisodesRecyclerViewAdapter : ListAdapter<Episode, EpisodesRecyclerViewAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)

        holder.bind(episode, View.OnClickListener {
            val direction = HomeFragmentDirections.openEpisodeDetailsAction(episode._id)
            it.findNavController().navigate(direction)
        })
    }

    class EpisodeViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {

            fun newInstance(parent: ViewGroup): EpisodeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_episode, parent, false)
                return EpisodeViewHolder(view)
            }
        }

        fun bind(episode: Episode, listener: View.OnClickListener) {
            val context = itemView.context

            itemView.titleTextView.text = episode.title?.rendered
            itemView.descriptionTextView.text = episode.excerpt?.rendered

            // FIXME :: Format date
            itemView.dateTextView.text = episode.date

            // FIXME :: Add placeholder and error messages
            Glide.with(context).load(episode.featuredImage).into(itemView.episodeImageView)

            itemView.setOnClickListener(listener)
        }

    }
}