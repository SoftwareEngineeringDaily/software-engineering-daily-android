package com.koalatea.sedaily.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.PlaybackActivity
import com.koalatea.sedaily.ViewModelFactory
import com.koalatea.sedaily.databinding.FragmentDownloadsBinding

class DownloadsFragment : Fragment() {
    private lateinit var binding: FragmentDownloadsBinding
    private lateinit var viewModel: DownloadsViewModel
    private var errorSnackbar: Snackbar? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false)

        binding.postList.layoutManager = LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
//
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(DownloadsViewModel::class.java)
        viewModel.errorMessage.observe(this, Observer {
            errorMessage -> if(errorMessage != null) showError(errorMessage) else hideError()
        })

        // Play listener
        viewModel.playRequested.observe(this, Observer {
            (this.activity as PlaybackActivity).playMedia(it)
        })

        binding.viewModel = viewModel

        return binding.root
    }

    private fun showError(@StringRes errorMessage:Int){
        errorSnackbar = Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_INDEFINITE)
//        errorSnackbar?.setAction(R.string.retry, viewModel.errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError(){
        errorSnackbar?.dismiss()
    }
}
