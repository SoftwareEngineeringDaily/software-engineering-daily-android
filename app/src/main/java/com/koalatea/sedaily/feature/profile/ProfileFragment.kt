package com.koalatea.sedaily.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.koalatea.sedaily.R
import com.koalatea.sedaily.model.Profile
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.AlertDialogFragment
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROMPT_LOGIN = "prompt_login_dialog"

class ProfileFragment : BaseFragment() {

    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        viewModel.profileResource.observe(this, Observer { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success<Profile> -> renderProfile(resource.data)
                    is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
                }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                AlertDialogFragment.show(
                        requireFragmentManager(),
                        message = getString(R.string.prompt_login),
                        positiveButton = getString(R.string.ok),
                        tag = TAG_DIALOG_PROMPT_LOGIN)
            }
        })

        viewModel.fetchProfile()
    }

    private fun showLoading() {
    }

    private fun renderProfile(profile: Profile) {
    }

}