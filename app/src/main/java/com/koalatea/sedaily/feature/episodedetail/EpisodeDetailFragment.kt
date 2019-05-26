package com.koalatea.sedaily.feature.episodedetail

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
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.koalatea.sedaily.PlaybackActivity
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ViewModelFactory
import com.koalatea.sedaily.databinding.FragmentEpisodeDetailBinding

class EpisodeDetailFragment : Fragment() {

    lateinit var episodeId: String
    var detailViewModel: EpisodeDetailViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val safeArgs: EpisodeDetailFragmentArgs by navArgs()
        episodeId = safeArgs.episodeId

        detailViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity))
                .get(EpisodeDetailViewModel::class.java)
        detailViewModel?.loadEpisode(episodeId)
//        viewModel.errorMessage.observe(this, Observer {
//            errorMessage -> if(errorMessage != null) showError(errorMessage) else hideError()
//        })

        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.visibility = View.VISIBLE
        fab?.setOnClickListener {
            detailViewModel?.playRequest()
        }

        val binding = DataBindingUtil.inflate<FragmentEpisodeDetailBinding>(
                inflater, R.layout.fragment_episode_detail, container, false
        ).apply {
            viewModel = detailViewModel
            lifecycleOwner = this@EpisodeDetailFragment
//            fab.setOnClickListener{}
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

        detailViewModel?.playRequested?.observe(this, Observer {
            (this.activity as PlaybackActivity).playMedia(it)
        })

        return binding.root
    }

    private fun queryRemoveDownload() {
        AlertDialog.Builder(this.context!!)
                .setTitle("SoftwareDaily")
                .setMessage("Do you really want to remove this download?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { _, _ -> removeDownloadFromDB() }
                .setNegativeButton(android.R.string.no, null).show()
    }

    private fun removeDownloadFromDB() {
        detailViewModel?.removeDownloadForId(episodeId)
    }
}