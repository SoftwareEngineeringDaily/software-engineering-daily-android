package com.koalatea.sedaily.feature.relatedlinks

import android.os.Bundle
import android.view.*
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
import com.koalatea.sedaily.util.openUrl
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_related_links.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"
private const val TAG_DIALOG_ADD_RELATED_LINK = "add_related_link"

class RelatedLinksFragment : BaseFragment(), AddRelatedLinkDialogFragment.OnAddRelatedLinkListener {

    override val episodeId: String?
        get() = viewModel.episodeId

    private val viewModel: RelatedLinksViewModel by viewModel()

    private var relatedLinksEpoxyController: RelatedLinksEpoxyController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

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
                    if (!openUrl(url)) {
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

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                showPromptLoginDialog()
            }
        })

        viewModel.navigateToAddRelatedLink.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                AddRelatedLinkDialogFragment.show(this, requireFragmentManager(), TAG_DIALOG_ADD_RELATED_LINK)
            }
        })

        viewModel.fetchRelatedLinks(episodeId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_related_links, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                viewModel.addRelatedLink()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onAddRelatedLinkSuccess() {
        acknowledgeAddRelatedLinkSuccess()

        viewModel.reloadRelatedLinks()
    }

    override fun onAddRelatedLinkFailure(e: Exception?, isConnected: Boolean?) {
        if (isConnected == true) {
            acknowledgeConnectionError()
        } else {
            acknowledgeAddRelatedLinkFailed()
        }
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

    private fun acknowledgeAddRelatedLinkSuccess()
            = Snackbar.make(requireView(), R.string.add_related_link_success, Snackbar.LENGTH_SHORT).show()

    private fun acknowledgeAddRelatedLinkFailed()
            = Snackbar.make(requireView(), R.string.add_related_link_failed, Snackbar.LENGTH_SHORT).show()

    private fun acknowledgeViewRelatedLinkFailed()
            = Snackbar.make(requireView(), R.string.view_related_link_failed, Snackbar.LENGTH_SHORT).show()

}