package com.koalatea.sedaily.network

import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.model.Profile
import com.koalatea.sedaily.network.response.AuthResponse
import com.koalatea.sedaily.network.response.CommentsResponse
import com.koalatea.sedaily.network.response.FavoriteResponse
import com.koalatea.sedaily.network.response.VoteResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface SEDailyApi {

    @GET("posts")
    fun getEpisodesAsync(
            @Query("search") searchTerm: String? = null,
            @Query("categories") categoryId: String? = null,
            @Query("tags") tagId: String? = null,
            @Query("createdAtBefore") createdAtBefore: String? = null,
            @Query("limit") pageSize: Int): Deferred<Response<List<Episode>>>

    @GET("posts/{episode_id}")
    fun getEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<Episode>>

    @POST("posts/{episode_id}/favorite")
    fun favoriteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<FavoriteResponse>>

    @POST("posts/{episode_id}/unfavorite")
    fun unfavoriteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<FavoriteResponse>>

    @POST("posts/{episode_id}/upvote")
    fun upvoteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<VoteResponse>>

    @POST("posts/{episode_id}/downvote")
    fun downvoteEpisodeAsync(@Path("episode_id") episode_id: String): Deferred<Response<VoteResponse>>

    @GET("comments/forEntity/{entity_id}")
    fun getEpisodeCommentsAsync(@Path("entity_id") entityId: String): Deferred<Response<CommentsResponse>>

//    var params = [String: String]()
//    params[Params.commentContent] = commentContent
//    params[Params.entityType] = "forumthread"

//    @POST("comments/forEntity/{entity_id}")
//    fun addEpisodeCommentAsync(@Path("entity_id") entityId: String, ): Deferred<Response<List<Comment>>>

    @FormUrlEncoded
    @POST("auth/login")
    fun loginAsync(@Field("username") usernameOrEmail: String, @Field("password") password: String): Deferred<Response<AuthResponse>>

    @FormUrlEncoded
    @POST("auth/register")
    fun registerAsync(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Deferred<Response<AuthResponse>>

    @GET("users/me")
    fun getProfileAsync(): Deferred<Response<Profile>>

}