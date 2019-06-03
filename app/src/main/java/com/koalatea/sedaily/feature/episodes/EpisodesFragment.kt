package com.koalatea.sedaily.feature.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.episodes.epoxy.EpisodesEpoxyController
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.NetworkState
import kotlinx.android.synthetic.main.fragment_episodes.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class EpisodesFragment : Fragment() {

    companion object {
        fun newInstance(categoryId: String?): EpisodesFragment {
            val fragment = EpisodesFragment()
            fragment.arguments = EpisodesFragmentArgs.Builder(categoryId).build().toBundle()

            return fragment
        }
    }

    private val viewModel: EpisodesViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_episodes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: EpisodesFragmentArgs by navArgs()
        val categoryId = safeArgs.categoryId

        epoxyRecyclerView.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        epoxyRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        val episodesEpoxyController = EpisodesEpoxyController()
        epoxyRecyclerView.setControllerAndBuildModels(episodesEpoxyController)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.fetchPosts(SearchQuery(categoryId = categoryId))
        viewModel.episodesPagedList.observe(this, Observer { results ->
            episodesEpoxyController.submitList(results)
            epoxyRecyclerView.requestModelBuild()
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                is NetworkState.Error -> {
                    // TODO :: Handle error.
//                    showError(it.message)
                }
                else -> {
                }// Ignore
            }
        })

        viewModel.refreshState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it == NetworkState.Loading

            when (it) {
                NetworkState.Loaded -> {
                } // TODO :: Handle empty state
                else -> {
                }// Ignore
            }
        })


//        viewModel.episodes.observe(this, Observer { results ->
//            if (results != null && results.isNotEmpty())
//                adapter.submitList(results)
//        })
//        viewModel.errorMessage.observe(this, Observer { errorMessage ->
//            if (errorMessage != null) showError(errorMessage) else hideError()
//        })
//
//        viewModel.playRequested.observe(this, Observer {
//            (this.activity as PlaybackActivity).playMedia(it)
//        })
//
//        val disposable = episodesSearchRepository
//                .getSearchChange
//                .subscribe { query ->
//                    viewModel.performSearch(query)
//                }
//        compositeDisposable.add(disposable)
//
//        val query = episodesSearchRepository.currentSearch
//        if (query.isEmpty()) {
//            viewModel.loadHomeFeed()
//        } else {
//            viewModel.performSearch(query)
//        }
    }

    private fun showError(@StringRes errorMessage: Int) {
        view?.let {
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

}
