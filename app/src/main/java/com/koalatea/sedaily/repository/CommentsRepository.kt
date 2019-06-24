package com.koalatea.sedaily.repository

import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsRepository(private val api: SEDailyApi) {

//    fun fetchComments(episodeId: String): PagedResult<Comment> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    // FIXME :: Remove and return a results instead
    suspend fun fetchComments(entityId: String) = withContext(Dispatchers.IO) {
        safeApiCall { api.getEpisodeCommentsAsync(entityId).await() }?.body()!!.result
    }

}