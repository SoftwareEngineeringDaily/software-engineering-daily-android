package com.koalatea.sedaily.network

import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.User
import com.koalatea.sedaily.model.response.FavoriteResponse
import com.koalatea.sedaily.model.response.VoteResponse
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface SEDailyApi {

    @GET("posts")
    fun getEpisodesAsync(
            @Query("search") searchTerm: String? = null,
            @Query("categories") categoryId: String? = null,
            @Query("createdAtBefore") createdAtBefore: String? = null,
            @Query("limit") pageSize: Int): Deferred<Response<List<Episode>>>

//    func getComments(rootEntityId: String, onSuccess: @escaping ([Comment]) -> Void,
//        let urlString = self.rootURL + Endpoints.comments + "/forEntity/" + rootEntityId

//    func createComment(rootEntityId: String, parentComment: Comment?, commentContent: String, onSuccess: @escaping () -> Void,
//        let urlString = self.rootURL + Endpoints.comments + "/forEntity/" + rootEntityId

    @POST("posts/{episode_id}/favorite")
    fun favoriteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<FavoriteResponse>>

    @POST("posts/{episode_id}/unfavorite")
    fun unfavoriteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<FavoriteResponse>>

    @POST("posts/{episode_id}/upvote")
    fun upvoteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<VoteResponse>>

    @POST("posts/{episode_id}/downvote")
    fun downvoteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<VoteResponse>>

    @FormUrlEncoded
    @POST("auth/login")
    fun login(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

    @FormUrlEncoded
    @POST("auth/register")
    fun register(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

}