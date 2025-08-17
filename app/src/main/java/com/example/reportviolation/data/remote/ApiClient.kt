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
import java.nio.charset.StandardCharsets
import okhttp3.MultipartBody
import okio.Buffer

object ApiClient {
    // Reads from BuildConfig set by productFlavors (lan/emulator)
    private val BASE_URL: String = BuildConfig.BASE_URL

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val token = TokenStore.accessToken
        val newReq = if (!token.isNullOrBlank()) {
            println("AUTH_INTERCEPTOR add Authorization Bearer tokenPresent=true path=" + req.url.encodedPath)
            req.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            println("AUTH_INTERCEPTOR no token path=" + req.url.encodedPath)
            req
        }
        chain.proceed(newReq)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val debugPayloadInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startNs = System.nanoTime()
        if (BuildConfig.DEBUG) {
            try {
                val method = request.method
                val path = request.url.encodedPath
                val body = request.body
                var bodyLog = ""
                if (body != null) {
                    bodyLog = when (body) {
                        is MultipartBody -> {
                            val names = body.parts.mapNotNull { part ->
                                part.headers?.get("Content-Disposition")
                            }
                            "<multipart parts=" + names.size + "> " + names.joinToString(", ")
                        }
                        else -> {
                            val buffer = Buffer()
                            body.writeTo(buffer)
                            val charset = body.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                            val raw = buffer.readString(charset)
                            // Try to pretty print JSON
                            try {
                                val jsonElement = com.google.gson.JsonParser.parseString(raw)
                                com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
                            } catch (_: Throwable) {
                                raw
                            }
                        }
                    }
                }
                println("API_REQ method=" + method + " path=" + path + if (bodyLog.isNotBlank()) "\npayload=" + bodyLog else "")
            } catch (_: Throwable) { }
        }
        val response = chain.proceed(request)
        if (BuildConfig.DEBUG) {
            try {
                val tookMs = (System.nanoTime() - startNs) / 1_000_000
                val path = request.url.encodedPath
                val code = response.code
                val peek = response.peekBody(1024 * 1024L)
                val raw = peek.string()
                val pretty = try {
                    val jsonElement = com.google.gson.JsonParser.parseString(raw)
                    com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
                } catch (_: Throwable) { raw }
                println("API_RES code=" + code + " path=" + path + " tookMs=" + tookMs + "\nbody=" + pretty)
            } catch (_: Throwable) { }
        }
        response
    }

    private val baseClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(debugPayloadInterceptor)
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


