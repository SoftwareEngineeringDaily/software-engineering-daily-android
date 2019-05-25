package com.koalatea.sedaily.feature.downloadList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.model.DownloadDao

class DownloadViewModel : ViewModel() {
    private val title = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()

    fun bind(episode: DownloadDao.DownloadEpisode) {
        title.value = episode.title
        postImage.value = episode.featuredImage
    }

    fun getPostTitle(): MutableLiveData<String> {
        return title
    }

    fun getPostImage(): MutableLiveData<String> {
        return postImage
    }
}