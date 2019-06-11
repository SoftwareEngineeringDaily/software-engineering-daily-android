package com.koalatea.sedaily.feature.episodedetail

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.database.table.Episode
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.util.supportActionBar
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.fragment_episode_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodeDetailFragment : Fragment() {

    private val viewModel: EpisodeDetailViewModel by viewModel()

    lateinit var episodeId: String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_episode_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: EpisodeDetailFragmentArgs by navArgs()
        episodeId = safeArgs.episodeId

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        downloadButton.setOnClickListener {
            Permissions.check(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.rationale_storage_permission), object : PermissionHandler() {
                override fun onGranted() {
                    downloadButton.isEnabled = false

                    viewModel.download()
                }
            })
        }

        deleteButton.setOnClickListener {
            promptDeleteDownload { viewModel.delete() }
        }

        likesButton.setOnClickListener { viewModel.toggleUpvote() }
        bookmarkButton.setOnClickListener { viewModel.toggleBookmark() }

        viewModel.episodeDetailsResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success<Episode> -> renderEpisode(resource.data)
                is Resource.Error -> acknowledgeGenericError()
            }
        })

        viewModel.downloadStatusLiveData.observe(this, Observer {
            val downloadStatus = it.peekContent()
            when(downloadStatus) {
                is DownloadStatus.Initial -> showDownloadViews()
                is DownloadStatus.Unknown -> showDownloadViews()
                is DownloadStatus.Downloading -> showDownloadProgress(downloadStatus.progress)
                is DownloadStatus.Downloaded -> showDeleteViews()
                is DownloadStatus.Error -> showDownloadViews()
            }

            if (it.userAction && !it.handleIfNotHandled()) {
                when (downloadStatus) {
                    is DownloadStatus.Initial -> showDownloadViews()
                    is DownloadStatus.Unknown -> acknowledgeDownloadFailed()
                    is DownloadStatus.Downloading -> showDownloadProgress(downloadStatus.progress)
                    is DownloadStatus.Downloaded -> acknowledgeDownloadSucceeded()
                    is DownloadStatus.Error -> acknowledgeDownloadFailed()
                }
            }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
//                val direction = EpisodeDetailFragmentDirections.openCommentsAction(episodeId)
//                findNavController().navigate(direction)
                Toast.makeText(context, "Debug :: Login first", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.upvoteLiveData.observe(this, Observer {
            val upvoteEvent = it.peekContent()
            likesButton.setIconResource(if (upvoteEvent.upvoted) R.drawable.vd_favorite else R.drawable.vd_favorite_border)
            likesButton.text = if (upvoteEvent.score > 0) upvoteEvent.score.toString() else ""

            if (it.userAction && !it.handleIfNotHandled()) {
                if (upvoteEvent.failed) {
                    acknowledgeGenericError()
                }
            }
        })

        viewModel.bookmarkLiveData.observe(this, Observer {
            val bookmarkEvent = it.peekContent()
            bookmarkButton.setIconResource(if (bookmarkEvent.bookmarked == true) R.drawable.vd_bookmark else R.drawable.vd_bookmark_border)

            if (it.userAction && !it.handleIfNotHandled()) {
                if (bookmarkEvent.failed) {
                    acknowledgeGenericError()
                }
            }
        })

        viewModel.fetchEpisodeDetails(episodeId)
    }

    private fun showDownloadProgress(progress: Float) {
        downloadProgressBar.progress = progress.toInt()
        downloadProgressBar.visibility = View.VISIBLE
    }

    private fun showLoading() {
        headerCardView.visibility = View.GONE
        detailsCardView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun renderEpisode(episode: Episode) {
        renderTags(episode)

        supportActionBar?.title = episode.titleString
        episodeTitleTextView.text = episode.titleString

        context?.let { context ->
            Glide.with(context)
                    .load(episode.httpsGuestImageUrl)
                    .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                    .placeholder(R.drawable.vd_image)
                    .error(R.drawable.vd_broken_image)
                    .into(guestImageView)
        }

        dateTextView.text = DateFormat.getDateFormat(context).format(episode.utcDate)

        renderContent(episode)

        commentsButton.text = episode.thread?.commentsCount?.let { if (it > 0) it.toString() else "" }
        commentsButton.setOnClickListener {
            episode.thread?._id?.let { threadId ->
                val direction = EpisodeDetailFragmentDirections.openCommentsAction(threadId)
                findNavController().navigate(direction)
            } ?: acknowledgeGenericError()
        }

        // Hide loading view and show content.
        progressBar.visibility = View.GONE
        headerCardView.visibility = View.VISIBLE
        detailsCardView.visibility = View.VISIBLE
    }

    private fun renderTags(episode: Episode) {
        if (episode.filterTags.isNullOrEmpty()) {
            tagsHorizontalScrollView.visibility = View.GONE
        } else {
            tagsChipGroup.removeAllViews()

            episode.filterTags.forEach { tag ->
                tagsChipGroup.addView(Chip(context).apply {
                    text = tag.name

                    setOnClickListener {
                        val direction = EpisodeDetailFragmentDirections.openEpisodesAction(SearchQuery(tagId = tag.id.toString()))
                        findNavController().navigate(direction)
                    }
                })
            }

            tagsHorizontalScrollView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun renderContent(episode: Episode) {
        episode.content?.rendered?.let { html ->
            contentWebView.settings.javaScriptEnabled = true
            contentWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.loadUrl("javascript:(function() { " +
                            "var head = document.getElementsByClassName('powerpress_player')[0].style.display='none'; " +
                            "var head = document.getElementsByClassName('powerpress_links')[0].style.display='none'; " +
                            "var head = document.getElementsByClassName('wp-image-2475')[0].style.display='none'; " +
                            "})()")
                }
            }

            contentWebView.settings.defaultTextEncodingName = "utf-8"
            contentWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
    }

    private fun showDownloadViews() {
        deleteButton.visibility = View.GONE
        downloadButton.isEnabled = true
        downloadButton.visibility = View.VISIBLE
        downloadProgressBar.visibility = View.INVISIBLE
    }

    private fun showDeleteViews() {
        deleteButton.visibility = View.VISIBLE
        downloadButton.visibility = View.INVISIBLE
        downloadProgressBar.visibility = View.INVISIBLE
    }

    private fun acknowledgeGenericError() = Snackbar.make(containerConstraintLayout, R.string.error_generic, Snackbar.LENGTH_SHORT).show()

    private fun acknowledgeDownloadSucceeded() = Snackbar.make(containerConstraintLayout, R.string.episode_download_succeeded, Snackbar.LENGTH_SHORT).show()
    private fun acknowledgeDownloadFailed() = Snackbar.make(containerConstraintLayout, R.string.episode_download_failed, Snackbar.LENGTH_SHORT).show()
    private fun promptDeleteDownload(positiveCallback: () -> Unit) {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.episode_delete_download_prompt)
                .setPositiveButton(R.string.yes) { _, _ -> positiveCallback() }
                .setNegativeButton(R.string.no, null)
                .show()
    }

}