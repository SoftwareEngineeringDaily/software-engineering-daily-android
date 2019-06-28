package com.koalatea.sedaily.feature.commentList

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Comment
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.CommentsRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommentsViewModel(
        private val commentsRepository: CommentsRepository
) : ViewModel() {

    private val commentsLiveData = MutableLiveData<String>()
    val commentsResource: LiveData<Resource<List<Comment>>> = Transformations.switchMap(commentsLiveData) { entityId ->
        liveData {
            emit(Resource.Loading)

            emit(commentsRepository.fetchComments(entityId))
        }
    }

    private val _navigateToLogin = MutableLiveData<Event<String>>()
    val navigateToLogin: LiveData<Event<String>>
        get() = _navigateToLogin

    @MainThread
    fun fetchComments(entityId: String) {
        commentsLiveData.value = entityId
    }

    @MainThread
    fun addComment(comment: String) {

    }

    @MainThread
    fun cancelReply() {

    }

    @MainThread
    fun replyTo(comment: Comment) {

    }

}
