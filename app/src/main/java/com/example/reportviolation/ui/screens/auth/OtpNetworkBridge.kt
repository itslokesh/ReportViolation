package com.example.reportviolation.ui.screens.auth

import android.content.Context
import com.example.reportviolation.data.remote.*
import com.example.reportviolation.data.remote.auth.TokenStore
import okhttp3.OkHttpClient

object OtpNetworkBridge {
    // Build Retrofit with authenticator pipeline
    private val authRetrofit by lazy {
        val baseRetrofit = ApiClient.retrofit(OkHttpClient.Builder().build())
        val authApi = baseRetrofit.create(AuthApi::class.java)
        val client = ApiClient.buildClientWithAuthenticator(authApi)
        ApiClient.retrofit(client)
    }

    private val authApi: AuthApi by lazy { authRetrofit.create(AuthApi::class.java) }
    private val uploadApi: UploadApi by lazy { authRetrofit.create(UploadApi::class.java) }
    private val reportsApi: CitizenReportsApi by lazy { authRetrofit.create(CitizenReportsApi::class.java) }
    private val gson by lazy { com.google.gson.Gson() }

    suspend fun sendOtp(fullPhone: String): Boolean {
        val payload = OtpSendBody(fullPhone)
        println("API_SEND_OTP payload=" + gson.toJson(payload))
        val res = authApi.sendOtp(payload)
        return res.success
    }

    suspend fun verifyOtp(fullPhone: String, otp: String): Boolean {
        val payload = OtpVerifyBody(fullPhone, otp)
        println("API_VERIFY_OTP payload=" + gson.toJson(payload))
        val res = authApi.verifyOtp(payload)
        val data = res.data ?: return false
        TokenStore.update(data.token, data.refreshToken)
        return true
    }

    suspend fun uploadPhoto(context: Context, uri: android.net.Uri): String? {
        val part = uriToPart(context, uri, "photo")
        val res = uploadApi.uploadPhoto(part)
        return res.data?.url?.let { ensureAbsolute("http://192.168.29.250:3000", it) }
    }

    suspend fun uploadVideo(context: Context, uri: android.net.Uri): String? {
        val part = uriToPart(context, uri, "video")
        val res = uploadApi.uploadVideo(part)
        return res.data?.url?.let { ensureAbsolute("http://192.168.29.250:3000", it) }
    }

    suspend fun submitReport(body: ReportCreateBody): Boolean {
        println("API_CREATE_REPORT payload=" + gson.toJson(body))
        val res = reportsApi.createReport(body)
        return res.success
    }
}


