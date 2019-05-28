package com.koalatea.sedaily.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.PlaybackActivity
import com.koalatea.sedaily.databinding.FragmentMainBinding
import com.koalatea.sedaily.feature.downloader.DownloadRepository
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private val downloadRepository: DownloadRepository by inject()
    private val viewModel: HomeFeedViewModel by viewModel()

    private lateinit var binding: FragmentMainBinding
    private var errorSnackbar: Snackbar? = null
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.hasPlantings = false

        binding.postList.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                recyclerView.layoutManager.apply {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (totalItemCount == lastVisibleItemPosition + 1) {
                        binding.viewModel?.loadHomeFeedAfter()
//                    binding.postList.removeOnScrollListener(scrollListener)
                    }
                }
            }
        }
        binding.postList.addOnScrollListener(scrollListener)

        val adapter = HomeFeedListAdapter(viewModel, downloadRepository)
        binding.postList.adapter = adapter

        viewModel.episodes.observe(this, Observer { results ->
            if (results != null && results.isNotEmpty())
                adapter.submitList(results)
        })
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) showError(errorMessage) else hideError()
        })

        viewModel.playRequested.observe(this, Observer {
            (this.activity as PlaybackActivity).playMedia(it)
        })

        val disposable = PodcastSearchRepository
                .getInstance().getSearchChange
                .subscribe { query ->
                    viewModel.performSearch(query)
                }
        compositeDisposable.add(disposable)

        binding.viewModel = viewModel

        val query = PodcastSearchRepository.getInstance().currentSearch
        if (query.isEmpty()) {
            viewModel.loadHomeFeed()
        } else {
            viewModel.performSearch(query)
        }

        return binding.root
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun showError(@StringRes errorMessage: Int) {
        errorSnackbar = Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_INDEFINITE)
//        errorSnackbar?.setAction(R.string.retry, viewModel.errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError() {
        errorSnackbar?.dismiss()
    }
}
