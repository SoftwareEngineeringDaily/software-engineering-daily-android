package com.koalatea.sedaily.downloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.databinding.ItemDownloadBinding
import com.koalatea.sedaily.models.DownloadDao.DownloadEpisode
import com.koalatea.sedaily.models.DownloadDao

class DownloadsListAdapter(
    private val downloadsViewModel: DownloadsViewModel
): RecyclerView.Adapter<DownloadsListAdapter.ViewHolder>() {
    private lateinit var postList: List<DownloadDao.DownloadEpisode>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsListAdapter.ViewHolder {
        val binding: ItemDownloadBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_download, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadsListAdapter.ViewHolder, position: Int) {
        val episode = postList[position]
        holder.bind(createOnClickListener(episode.postId), createPlayClickListener(episode), episode)
    }

    override fun getItemCount(): Int {
        return if(::postList.isInitialized) postList.size else 0
    }

    private fun createOnClickListener(episodeId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = DownloadsFragmentDirections.ActionPlantListFragmentToPlantDetailFragment(episodeId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createPlayClickListener(episode: DownloadEpisode): View.OnClickListener {
        return View.OnClickListener {
            downloadsViewModel.play(episode)
        }
    }

    fun updateFeedList(postList: List<DownloadDao.DownloadEpisode>) {
        this.postList = postList
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemDownloadBinding): RecyclerView.ViewHolder(binding.root) {
        private val viewModel = DownloadViewModel()

        fun bind(listener: View.OnClickListener, playListener: View.OnClickListener, download: DownloadDao.DownloadEpisode) {
            viewModel.bind(download)
            binding.viewModel = viewModel
            binding.clickListener = listener
            binding.playClickListener = playListener
        }
    }
}