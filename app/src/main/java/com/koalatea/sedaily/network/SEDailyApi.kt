package com.koalatea.sedaily.network

import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.User
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface SEDailyApi {

    @GET("posts")
    fun getPosts(@QueryMap options: Map<String, String>): Observable<List<Episode>>// Deferred<List<Episode>>

    @FormUrlEncoded
    @POST("auth/login")
    fun login(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

    @FormUrlEncoded
    @POST("auth/register")
    fun register(@Field("username") username: String, @Field("email") email: String, @Field("password") password: String): Single<User>

}