package com.example.reportviolation.data.remote

import com.google.gson.annotations.SerializedName
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

data class CitizenRegisterBody(
    val phoneNumber: String,
    val name: String,
    val email: String,
)

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
    val photoUrl: String? = null,
    val videoUrl: String? = null,
)

// Backend list response shapes for citizen reports
data class ReportsPage(
    @SerializedName(value = "reports", alternate = ["items"])
    val reports: List<CitizenReportItem> = emptyList(),
    val pagination: Pagination
)

data class CitizenReportItem(
    val id: String,
    val violationTypes: List<String>? = null,
    val timestamp: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val pincode: String? = null,
    val city: String? = null,
    val district: String? = null,
    val state: String? = null,
    val status: String? = null,
    val photoUrl: String? = null,
    val videoUrl: String? = null,
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

data class CitizenReportDetail(
    val id: Long,
    val reporterId: String? = null,
    val reporterPhoneEncrypted: String? = null,
    val reporterPhoneHash: String? = null,
    val reporterCity: String? = null,
    val reporterPincode: String? = null,
    val violationType: String? = null,
    val severity: String? = null,
    val description: String? = null,
    val timestamp: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressEncrypted: String? = null,
    val pincode: String? = null,
    val city: String? = null,
    val district: String? = null,
    val state: String? = null,
    val vehicleNumberEncrypted: String? = null,
    val vehicleType: String? = null,
    val vehicleColor: String? = null,
    val photoUrl: String? = null,
    val videoUrl: String? = null,
    val mediaMetadata: String? = null,
    val status: String? = null,
    val isDuplicate: Boolean? = null,
    val duplicateGroupId: String? = null,
    val confidenceScore: Double? = null,
    val reviewerId: String? = null,
    val reviewTimestamp: String? = null,
    val reviewNotes: String? = null,
    val challanIssued: Boolean? = null,
    val challanNumber: String? = null,
    val pointsAwarded: Int? = null,
    val isFirstReporter: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isAnonymous: Boolean? = null,
    val citizenId: String? = null,
    val reviewer: Reviewer? = null,
)

data class Reviewer(
    val id: String? = null,
    val name: String? = null,
    val badgeNumber: String? = null,
)


