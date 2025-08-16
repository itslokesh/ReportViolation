package com.example.reportviolation.data.remote

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface AuthApi {
    @POST("/api/auth/police/login")
    suspend fun login(@Body body: Map<String, String>): ApiResponse<LoginData>

    @POST("/api/auth/refresh")
    fun refresh(@Body body: RefreshBody): Call<ApiResponse<Tokens>>

    // Citizen OTP
    @POST("/api/auth/citizen/send-otp")
    suspend fun sendOtp(@Body body: OtpSendBody): ApiResponse<Any>

    @POST("/api/auth/citizen/verify-otp")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): ApiResponse<CitizenAuthData>
}

interface UploadApi {
    @Multipart
    @POST("/api/upload/photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): ApiResponse<UploadResult>

    @Multipart
    @POST("/api/upload/video")
    suspend fun uploadVideo(@Part video: MultipartBody.Part): ApiResponse<UploadResult>
}

interface CitizenReportsApi {
    @POST("/api/citizen/reports")
    suspend fun createReport(@Body body: ReportCreateBody): ApiResponse<Any>

    @GET("/api/citizen/reports")
    suspend fun listReports(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<Any>

    @GET("/api/citizen/reports/{id}")
    suspend fun getReport(@Path("id") id: String): ApiResponse<Any>
}


