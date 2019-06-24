package com.koalatea.sedaily.feature.profile

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.koalatea.sedaily.R
import com.koalatea.sedaily.model.Profile
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.fragment.BaseFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.include_empty_state.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment() {

    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        logoutButton.setOnClickListener {
            viewModel.logout()
        }

        viewModel.profileResource.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.RequireLogin -> showLoginEmptyState()
                is Resource.Success<Profile> -> renderProfile(resource.data)
                is Resource.Error -> if (resource.isConnected) acknowledgeGenericError() else acknowledgeConnectionError()
            }
        })

        viewModel.fetchProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login -> {
                val direction = ProfileFragmentDirections.openAuthAction()
                findNavController().navigate(direction)

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.login).isVisible = !viewModel.isUserLoggedIn
    }

    private fun showLoading() {
        emptyStateContainer.visibility = View.GONE
        profileDetailsContainer.visibility = View.GONE

        progressBar.visibility = View.VISIBLE
    }

    private fun showLoginEmptyState() {
        profileDetailsContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        // Update menu.
        activity?.invalidateOptionsMenu()

        emptyStateContainer.textView.text = getString(R.string.login_to_manage_profile)
        emptyStateContainer.visibility = View.VISIBLE
    }

    private fun renderProfile(profile: Profile) {
        usernameTextView.text = profile.username

        // Update menu.
        activity?.invalidateOptionsMenu()

        emptyStateContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        profileDetailsContainer.visibility = View.VISIBLE
    }

}