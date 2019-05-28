package com.koalatea.sedaily.koin.module

import com.koalatea.sedaily.BuildConfig
import com.koalatea.sedaily.feature.auth.UserRepository
import com.koalatea.sedaily.network.SEDailyApi
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private const val BASE_URL: String = "https://software-enginnering-daily-api.herokuapp.com/api/"

val networkModule = module {

    single<OkHttpClient> { //(userRepository: UserRepository) ->
        val userRepository = get<UserRepository>()

        val clientBuilder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val ongoing = chain.request().newBuilder()
                    ongoing.addHeader("Accept", "application/json;versions=1")

                    userRepository.token?.let { token ->
                        if (token.isNotBlank()) {
                            ongoing.addHeader("Authorization", "Bearer $token")
                        }
                    }

                    chain.proceed(ongoing.build())
                }

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(logging)
        }

        clientBuilder.build()
    }

    single<SEDailyApi> { //(okHttpClient: OkHttpClient) ->
        val okHttpClient = get<OkHttpClient>()
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClient)
                .build()
                .create(SEDailyApi::class.java)
    }

}