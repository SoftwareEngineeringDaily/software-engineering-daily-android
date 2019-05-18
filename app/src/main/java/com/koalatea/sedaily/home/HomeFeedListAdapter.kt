package com.koalatea.sedaily.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.databinding.ItemEpisodeBinding
import com.koalatea.sedaily.models.Episode

class HomeFeedListAdapter (
    private val homeFeedViewModel: HomeFeedViewModel
): ListAdapter<Episode, HomeFeedListAdapter.ViewHolder>(EpisodeDiffCallback()) {
    // @TODO: Currently public for HomeFeedModel,but we probably need a better way to get last element
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemEpisodeBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_episode, parent,
                false)
        return ViewHolder(binding, homeFeedViewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(createOnClickListener(episode._id), episode)
    }

    private fun createOnClickListener(episodeId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = MainFragmentDirections.ActionPlantListFragmentToPlantDetailFragment(episodeId)
            it.findNavController().navigate(direction)
        }
    }

    class ViewHolder(
        private val binding: ItemEpisodeBinding,
        private val homeFeedViewModel: HomeFeedViewModel
    ): RecyclerView.ViewHolder(binding.root) {
        private val viewModel = EpisodeViewModel(homeFeedViewModel)

        fun bind(listener: View.OnClickListener, episode: Episode) {
            viewModel.bind(episode)
            binding.viewModel = viewModel
            binding.clickListener = listener
        }
    }
}