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

    // Citizen profile registration/update
    @POST("/api/auth/citizen/register")
    suspend fun registerCitizen(@Body body: CitizenRegisterBody): ApiResponse<CitizenAuthData>

    // Profile endpoints
    @GET("/api/citizen/profile")
    suspend fun getCitizenProfile(): ApiResponse<CitizenProfile>
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

    @Multipart
    @POST("/api/citizen/reports")
    suspend fun createReportMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part photos: okhttp3.MultipartBody.Part? = null,
        @Part videos: okhttp3.MultipartBody.Part? = null,
    ): ApiResponse<Any>

    @GET("/api/citizen/reports")
    suspend fun listReports(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<ReportsPage>

    @GET("/api/citizen/reports/{id}")
    suspend fun getReport(@Path("id") id: String): ApiResponse<CitizenReportDetail>

    // Events timeline for a report
    @GET("/api/citizen/reports/{id}/events")
    suspend fun getReportEvents(@Path("id") id: String): ApiResponse<List<ReportEvent>>
}

interface FeedbackApi {
    @POST("/api/feedback/submit")
    suspend fun submit(@Body body: FeedbackSubmitRequest): ApiResponse<Any>

    @GET("/api/feedback/my-feedback")
    suspend fun listMine(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<FeedbackListPage>
}

interface CitizenNotificationsApi {
    @GET("/api/citizen/notifications")
    suspend fun listNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<NotificationsPage>

    @PATCH("/api/citizen/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): ApiResponse<Any>

    @PATCH("/api/citizen/notifications/read-all")
    suspend fun markAllRead(): ApiResponse<Any>
}

interface CitizenRewardsApi {
    @GET("/api/citizen/rewards/transactions")
    suspend fun listTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<RewardsTransactionsPage>
}


