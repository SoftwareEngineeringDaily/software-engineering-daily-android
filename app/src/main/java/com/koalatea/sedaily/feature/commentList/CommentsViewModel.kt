package com.koalatea.sedaily.feature.commentList

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.koalatea.sedaily.database.model.Comment
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.repository.CommentsRepository
import com.koalatea.sedaily.util.Event
import kotlinx.coroutines.launch
import timber.log.Timber

class CommentsViewModel(
        private val commentsRepository: CommentsRepository
) : ViewModel() {

    private val entityIdLiveData = MutableLiveData<String>()
    val commentsResource: LiveData<Resource<List<Comment>>> = Transformations.switchMap(entityIdLiveData) { entityId ->
        liveData {
            emit(Resource.Loading)

            emit(commentsRepository.fetchComments(entityId))
        }
    }

    private val _replyToCommentLiveData = MutableLiveData<Comment?>()
    val replyToCommentLiveData: LiveData<Comment?>
        get() = _replyToCommentLiveData

    private val _addCommentLiveData = MutableLiveData<Event<Resource<Boolean>>>()
    val addCommentLiveData: LiveData<Event<Resource<Boolean>>>
        get() = _addCommentLiveData

    @MainThread
    fun fetchComments(entityId: String) {
        entityIdLiveData.value = entityId
    }

    @MainThread
    fun addComment(comment: String) = viewModelScope.launch {
        if (comment.isBlank()) return@launch

        entityIdLiveData.value?.let { entityId ->
            val resource = commentsRepository.addComment(entityId, _replyToCommentLiveData.value?._id, comment)
            if (resource is Resource.Success) {
                cancelReply()
            }

            _addCommentLiveData.postValue(Event(resource))
        } ?: Timber.e("Cannot add comment, entityId is null")
    }

    @MainThread
    fun cancelReply() {
        _replyToCommentLiveData.value = null
    }

    @MainThread
    fun replyTo(comment: Comment) {
        _replyToCommentLiveData.value = comment
    }

}
