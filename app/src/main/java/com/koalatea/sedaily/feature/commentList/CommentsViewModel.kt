package com.koalatea.sedaily.feature.commentList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koalatea.sedaily.database.model.Comment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommentsViewModel(
        private val commentsRepository: CommentsRepository
) : ViewModel() {

    val commentsLiveData = MutableLiveData<List<Comment>>()

    fun fetchComments(entityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            commentsLiveData.value = commentsRepository.fetchComments(entityId)
        }
    }

}
