package com.koalatea.sedaily.network

import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.User
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface SEDailyApi {

    @GET("posts")
    fun getPostsAsync(
            @Query("search") searchTerm: String? = null,
            @Query("categories") categoryId: String? = null,
            @Query("createdAtBefore") createdAtBefore: String? = null,
            @Query("limit") pageSize: Int): Deferred<Response<List<Episode>>>

    @FormUrlEncoded
    @POST("auth/login")
    fun login(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

    @FormUrlEncoded
    @POST("auth/register")
    fun register(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

}