package com.koalatea.sedaily.episodedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ViewModelFactory
import com.koalatea.sedaily.databinding.FragmentEpisodeDetailBinding

class EpisodeDetailFragment : Fragment() {

    var episodeId: String? = null
    var detailViewModel: EpisodeDetailViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        episodeId = EpisodeDetailFragmentArgs.fromBundle(arguments).episodeId

        detailViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(EpisodeDetailViewModel::class.java)
        detailViewModel?.loadEpisode(episodeId!!)
//        viewModel.errorMessage.observe(this, Observer {
//            errorMessage -> if(errorMessage != null) showError(errorMessage) else hideError()
//        })

        val binding = DataBindingUtil.inflate<FragmentEpisodeDetailBinding>(
                inflater, R.layout.fragment_episode_detail, container, false
        ).apply {
            viewModel = detailViewModel
            setLifecycleOwner(this@EpisodeDetailFragment)
            fab.setOnClickListener{}
            removeDownload = View.OnClickListener {
                queryRemoveDownload()
            }
        }

        detailViewModel?.getPostContent()?.observe(this, Observer {
            val htmlContent = it
                    .replace(Regex("<audio class=\"wp-audio-shortcode\".*</audio>"), "")
                    .replace(Regex("<p class=\"powerpress_links powerpress_links_mp3\">.*Download</a></p>"), "")
            binding.plantDetail.loadData(htmlContent, "text/html", "UTF-8")
        })

        return binding.root
    }

    private fun queryRemoveDownload() {
        AlertDialog.Builder(this.context!!)
            .setTitle("SoftwareDaily")
            .setMessage("Do you really want to remove this download?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->  removeDownloadFromDB() }
            .setNegativeButton(android.R.string.no, null).show()
    }

    private fun removeDownloadFromDB() {
        episodeId?.apply {
            detailViewModel?.removeDownloadForId(this)
        }
    }
}