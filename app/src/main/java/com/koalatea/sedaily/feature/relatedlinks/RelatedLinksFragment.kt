package com.koalatea.sedaily.feature.relatedlinks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.relatedlinks.epoxy.RelatedLinksEpoxyController
import com.koalatea.sedaily.model.RelatedLink
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_related_links.epoxyRecyclerView
import kotlinx.android.synthetic.main.fragment_related_links.progressBar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"

class RelatedLinksFragment : BaseFragment() {

    private val viewModel: RelatedLinksViewModel by viewModel()

    private var relatedLinksEpoxyController: RelatedLinksEpoxyController? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_related_links, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: RelatedLinksFragmentArgs by navArgs()
        val episodeId = safeArgs.episodeId
        val transcriptUrl = safeArgs.episodeTranscriptUrl

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        epoxyRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        relatedLinksEpoxyController = RelatedLinksEpoxyController(
                resources,
                transcriptUrl,
                viewRelatedLinkClickListener = { url ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Timber.e(e)

                        acknowledgeViewRelatedLinkFailed()
                    }
                }
        ).apply {
            epoxyRecyclerView.setController(this)
        }

        viewModel.relatedLinksResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (relatedLinksEpoxyController?.currentData.isNullOrEmpty()) {
                        showLoading()
                    }
                }
                is Resource.Success -> renderRelatedLinks(resource.data)
                is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
            }
        })

        viewModel.addRelatedLinkLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { resource ->
//                addRelatedLinkButton.isEnabled = true

                when (resource) {
                    is Resource.RequireLogin -> showPromptLoginDialog()
//                    is Resource.Loading -> addRelatedLinkButton.isEnabled = false
                    is Resource.Success -> {
                        if (resource.data) {
//                            resetContent()
                            acknowledgeAddRelatedLinkSuccess()

                            // Reload links
                            viewModel.reloadRelatedLinks()
                        } else {
                            acknowledgeConnectionError()
                        }
                    }
                    is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
                }
            }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                showPromptLoginDialog()
            }
        })

        viewModel.fetchRelatedLinks(episodeId)
    }

    private fun showLoading() {
        epoxyRecyclerView.visibility = View.GONE

        progressBar.visibility = View.VISIBLE
    }

    private fun showPromptLoginDialog() {
        AlertDialogFragment.show(
                requireFragmentManager(),
                message = getString(R.string.prompt_login),
                positiveButton = getString(R.string.ok),
                tag = TAG_DIALOG_PROMPT_LOGIN)
    }

    private fun renderRelatedLinks(relatedLinks: List<RelatedLink>) {
        relatedLinksEpoxyController?.setData(relatedLinks)

        progressBar.visibility = View.GONE

        epoxyRecyclerView.visibility = View.VISIBLE
    }

    // TODO :: Add using a dialog.
//    private fun resetContent() {
////        titleEditText.text = null
////        urlEditText.text = null
//
//        activity?.hideKeyboard()
//    }

    private fun acknowledgeAddRelatedLinkSuccess()
            = Snackbar.make(requireView(), R.string.add_related_link_success, Snackbar.LENGTH_SHORT).show()

    private fun acknowledgeViewRelatedLinkFailed()
            = Snackbar.make(requireView(), R.string.view_related_link_failed, Snackbar.LENGTH_SHORT).show()

}