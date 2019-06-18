package com.koalatea.sedaily.feature.commentList

import com.koalatea.sedaily.database.model.Comment
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsRepository(private val api: SEDailyApi) {

//    fun fetchComments(episodeId: String): Result<Comment> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    // FIXME :: Remove and return a results instead
    suspend fun fetchComments(entityId: String): List<Comment>? {
        return withContext(Dispatchers.IO) {
            safeApiCall { api.getEpisodeCommentsAsync(entityId).await() }?.body()!!.result
        }
    }

}