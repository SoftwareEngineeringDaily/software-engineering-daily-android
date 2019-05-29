package com.koalatea.sedaily.feature.episodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.databinding.ItemEpisodeBinding
import com.koalatea.sedaily.feature.downloader.DownloadRepository
import com.koalatea.sedaily.model.Episode

class EpisodesRecyclerViewAdapter(
        private val episodesViewModel: EpisodesViewModel,
        private val downloadRepository: DownloadRepository
) : ListAdapter<Episode, EpisodesRecyclerViewAdapter.ViewHolder>(EpisodeDiffCallback()) {

    // @TODO: Currently public for HomeFeedModel,but we probably need a better way to get last element
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemEpisodeBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_episode, parent,
                false)
        return ViewHolder(binding, episodesViewModel, downloadRepository)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(createOnClickListener(episode._id), episode)

//        holder.itemView.findViewById<View>(R.id.play_button).setOnClickListener {
//            holder.itemView.findNavController().navigate(EpisodesFragmentDirections.openEpisodeDetailsAction(episode._id))
//        }
    }

    private fun createOnClickListener(episodeId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = EpisodesFragmentDirections.openEpisodeDetailsAction(episodeId)
            it.findNavController().navigate(direction)
        }
    }

    class ViewHolder(
            private val binding: ItemEpisodeBinding,
            episodesViewModel: EpisodesViewModel,
            downloadRepository: DownloadRepository
    ) : RecyclerView.ViewHolder(binding.root) {
        private val viewModel = EpisodeViewHolderViewModel(episodesViewModel, downloadRepository)

        fun bind(listener: View.OnClickListener, episode: Episode) {
            viewModel.bind(episode)
            binding.viewModel = viewModel
            binding.clickListener = listener
        }
    }
}