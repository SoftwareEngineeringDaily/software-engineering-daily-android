package com.koalatea.sedaily.feature.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
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

        postsRecyclerView.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        postsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        val adapter = EpisodesRecyclerViewAdapter()
        postsRecyclerView.adapter = adapter

        viewModel.fetchPosts(categoryId)
        viewModel.episodes.observe(this, Observer { results ->
            if (results != null && results.isNotEmpty())
                adapter.submitList(results)
        })

//        val scrollListener = object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                val totalItemCount = recyclerView.layoutManager?.itemCount
//                recyclerView.layoutManager.apply {
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                    if (totalItemCount == lastVisibleItemPosition + 1) {
//                        viewModel.loadHomeFeedAfter()
////                    binding.postList.removeOnScrollListener(scrollListener)
//                    }
//                }
//            }
//        }
//        postsRecyclerView.addOnScrollListener(scrollListener)
//
//        val adapter = EpisodesRecyclerViewAdapter(viewModel, downloadRepository)
//        postsRecyclerView.adapter = adapter
//
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
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG).show()
        } ?: Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

}
