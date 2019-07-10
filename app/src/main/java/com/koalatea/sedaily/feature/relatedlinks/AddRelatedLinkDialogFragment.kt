package com.koalatea.sedaily.feature.relatedlinks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.koalatea.sedaily.R
import com.koalatea.sedaily.network.Resource
import kotlinx.android.synthetic.main.dialog_fragment_add_related_link.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddRelatedLinkDialogFragment : AppCompatDialogFragment() {

    interface OnAddRelatedLinkListener {
        val episodeId: String?

        fun onAddRelatedLinkSuccess()
        fun onAddRelatedLinkFailure(e: Exception? = null, isConnected: Boolean? = null)
    }

    companion object {

        fun show(targetFragment: Fragment, fragmentManager: FragmentManager, tag: String) {
            val dialogFragment = fragmentManager.findFragmentByTag(tag)
            if (dialogFragment == null) {
                val dialog = AddRelatedLinkDialogFragment()
                dialog.setTargetFragment(targetFragment, 0)

                dialog.show(fragmentManager, tag)
            }
        }

    }

    private lateinit var listener: OnAddRelatedLinkListener

    private val viewModel: AddRelatedLinkViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            targetFragment is OnAddRelatedLinkListener -> this.listener = targetFragment as OnAddRelatedLinkListener
            activity is OnAddRelatedLinkListener -> this.listener = activity as OnAddRelatedLinkListener
            else -> throw IllegalArgumentException("Activity: $activity, or target fragment: $targetFragment doesn't implement ${OnAddRelatedLinkListener::class.java.name}")
        }

        isCancelable = false

        viewModel.validationLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { validationStatus ->
                titleTextInputEditText.error = if (validationStatus.isTitleValid) null else getString(R.string.invalid_title)
                urlTextInputEditText.error = if (validationStatus.isUrlValid) null else getString(R.string.invalid_url)
            }
        })

        viewModel.addRelatedLinkLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { resource ->
                addButton.isEnabled = true

                when (resource) {
                    Resource.RequireLogin -> listener.onAddRelatedLinkFailure()
                    is Resource.Loading -> addButton.isEnabled = false
                    is Resource.Success -> {
                        if (resource.data) {
                            listener.onAddRelatedLinkSuccess()
                        } else {
                            listener.onAddRelatedLinkFailure()
                        }

                        dialog?.dismiss()
                    }
                    is Resource.Error -> {
                        listener.onAddRelatedLinkFailure(resource.exception, resource.isConnected)

                        dialog?.dismiss()
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.dialog_fragment_add_related_link, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addButton.setOnClickListener {
            val title = titleTextInputEditText.text?.trim()?.toString() ?: ""
            val url = urlTextInputEditText.text?.trim()?.toString() ?: ""

            listener.episodeId?.let { episodeId ->
                viewModel.addRelatedLink(episodeId, title, url)
            } ?: listener.onAddRelatedLinkFailure()
        }

        cancelButton.setOnClickListener {
            dialog?.dismiss()
        }
    }

}