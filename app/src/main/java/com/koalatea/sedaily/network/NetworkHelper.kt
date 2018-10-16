package com.koalatea.sedaily.network

import com.koalatea.sedaily.BuildConfig
import com.koalatea.sedaily.auth.UserRepository
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL: String = "https://software-enginnering-daily-api.herokuapp.com/api/";

class NetworkHelper {
    companion object {
        fun getApi(): SEDailyApi  {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .client(provideOkhttpClient())
                .build()
                .create(SEDailyApi::class.java)
        }

        internal fun provideOkhttpClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor()

            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val userLogin = UserRepository.getInstance()

            val clientbuilder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val ongoing = chain.request().newBuilder()
                    ongoing.addHeader("Accept", "application/json;versions=1")

                    userLogin.getToken()?.apply {
                        if (userLogin.getToken() != "") {
                            ongoing.addHeader("Authorization", "Bearer ${userLogin.getToken()}")
                        }
                    }

                    chain.proceed(ongoing.build())
                }

            if (BuildConfig.DEBUG) {
                clientbuilder.addInterceptor(logging)
            }

            return clientbuilder.build()
        }
    }
}