package com.example.reportviolation.data.remote

import com.example.reportviolation.data.remote.auth.TokenAuthenticator
import com.example.reportviolation.data.remote.auth.TokenStore
import com.example.reportviolation.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Reads from BuildConfig set by productFlavors (lan/emulator)
    private val BASE_URL: String = BuildConfig.BASE_URL

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val token = TokenStore.accessToken
        val newReq = if (!token.isNullOrBlank())
            req.newBuilder().addHeader("Authorization", "Bearer $token").build()
        else req
        chain.proceed(newReq)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val baseClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    // Lazily constructed client once authenticator can be provided (after retrofit creates api)
    fun buildClientWithAuthenticator(refreshApi: AuthApi): OkHttpClient = baseClient.newBuilder()
        .authenticator(TokenAuthenticator(refreshApi))
        .build()

    fun retrofit(client: OkHttpClient): Retrofit = retrofitBuilder.client(client).build()
}


