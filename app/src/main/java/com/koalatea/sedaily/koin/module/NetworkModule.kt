package com.koalatea.sedaily.koin.module

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.koalatea.sedaily.BuildConfig
import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.repository.SessionRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

const val SEDAILY_URL = "http://softwareengineeringdaily.com"

// FIXME :: Move to debug build type
//private const val BASE_URL: String = "https://sedaily-backend-staging.herokuapp.com/api/"
private const val BASE_URL: String = "https://software-enginnering-daily-api.herokuapp.com/api/"

val networkModule = module {

    single { NetworkManager(androidApplication()) }

    single<OkHttpClient> {
        val userRepository = get<SessionRepository>()

        // Add security interceptor.
        val clientBuilder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    try {
                        val ongoing = chain.request().newBuilder()
                        ongoing.addHeader("Accept", "application/json;versions=1")

                        userRepository.token?.let { token ->
                            if (token.isNotBlank()) {
                                ongoing.addHeader("Authorization", "Bearer $token")
                            }
                        }

                        return@addInterceptor chain.proceed(ongoing.build())
                    } catch (e: Throwable) {
                        Timber.w(e)
                    }

                    return@addInterceptor chain.proceed(chain.request())
                }

        // Add debug interceptors.
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })

            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }

        clientBuilder.build()
    }

    single<Retrofit> {
        val okHttpClient = get<OkHttpClient>()
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(okHttpClient)
                .build()
    }

    single<SEDailyApi> {
        val retrofit = get<Retrofit>()
        retrofit.create(SEDailyApi::class.java)
    }

}