package com.koalatea.sedaily.downloadList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koalatea.sedaily.models.DownloadDao

class DownloadViewModel : ViewModel() {
    private val postId = MutableLiveData<String>()
    private val title = MutableLiveData<String>()
    private val filename = MutableLiveData<String>()
    private val postImage = MutableLiveData<String>()

    fun bind(episode: DownloadDao.DownloadEpisode) {
        postId.value = episode.postId
        title.value = episode.title
        filename.value = episode.filename
        postImage.value = episode.featuredImage
    }

    fun getPostId(): MutableLiveData<String> {
        return postId
    }

    fun getPostTitle(): MutableLiveData<String> {
        return title
    }

    fun getFileName(): MutableLiveData<String> {
        return filename
    }

    fun getPostImage(): MutableLiveData<String> {
        return postImage
    }
}