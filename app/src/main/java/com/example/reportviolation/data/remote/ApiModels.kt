package com.example.reportviolation.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?,
)

data class Tokens(val accessToken: String, val refreshToken: String)

data class LoginData(val user: Any?, val accessToken: String, val refreshToken: String)

data class UploadResult(
    val url: String,
    val filename: String,
    val size: Long,
    val mimeType: String,
)

data class OtpSendBody(val phoneNumber: String)
data class OtpVerifyBody(val phoneNumber: String, val otp: String)

data class CitizenAuthData(
    val token: String,
    val refreshToken: String,
    val user: Any?,
)

data class RefreshBody(val refreshToken: String)

data class ReportCreateBody(
    val violationTypes: List<String>,
    val severity: String? = null,
    val description: String? = null,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val pincode: String,
    val city: String,
    val district: String,
    val state: String,
    val vehicleNumber: String? = null,
    val vehicleType: String? = null,
    val vehicleColor: String? = null,
    val isAnonymous: Boolean = false,
)


