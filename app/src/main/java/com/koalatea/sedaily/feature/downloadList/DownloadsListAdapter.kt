package com.koalatea.sedaily.feature.downloadList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.koalatea.sedaily.R
import com.koalatea.sedaily.databinding.ItemDownloadBinding
import com.koalatea.sedaily.database.DownloadDao
import com.koalatea.sedaily.database.DownloadDao.DownloadEpisode

class DownloadsListAdapter(
        private val downloadsViewModel: DownloadsViewModel
) : RecyclerView.Adapter<DownloadsListAdapter.ViewHolder>() {
    private lateinit var postList: MutableList<DownloadDao.DownloadEpisode>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDownloadBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_download, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = postList[position]
        holder.bind(createOnClickListener(episode.postId),
                createPlayClickListener(episode),
                createRemoveClickListener(episode),
                episode)
    }

    override fun getItemCount(): Int {
        return if (::postList.isInitialized) postList.size else 0
    }

    fun removeItem(downloadId: String) {
        val download = postList.find { download -> download.postId == downloadId } ?: return

        val index = postList.indexOf(download)
        postList.removeAt(index)

        notifyItemRemoved(index)
    }

    private fun createOnClickListener(episodeId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = DownloadsFragmentDirections.openEpisodeDetailsAction(episodeId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createPlayClickListener(episode: DownloadEpisode): View.OnClickListener {
        return View.OnClickListener {
            downloadsViewModel.play(episode)
        }
    }

    private fun createRemoveClickListener(download: DownloadEpisode): View.OnClickListener {
        return View.OnClickListener {
            downloadsViewModel.requestRemoveDownload(download)
        }
    }

    fun updateFeedList(postList: MutableList<DownloadDao.DownloadEpisode>) {
        this.postList = postList
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        private val viewModel = DownloadViewModel()

        fun bind(listener: View.OnClickListener,
                 playListener: View.OnClickListener,
                 removeListener: View.OnClickListener,
                 download: DownloadDao.DownloadEpisode) {
            viewModel.bind(download)
            binding.viewModel = viewModel
            binding.clickListener = listener
            binding.playClickListener = playListener
            binding.removeClickListener = removeListener
        }
    }
}