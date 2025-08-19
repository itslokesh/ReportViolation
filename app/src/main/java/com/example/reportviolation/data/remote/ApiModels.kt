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

data class CitizenProfile(
    val id: String?,
    val phoneNumberEncrypted: String?,
    val name: String?,
    val emailEncrypted: String?,
    @SerializedName(value = "pointsEarned", alternate = ["totalPoints", "points", "rewardPoints"]) val pointsEarned: Int? = null,
)

data class RefreshBody(val refreshToken: String)

data class CitizenRegisterBody(
    val phoneNumber: String,
    val name: String,
    val email: String,
    // Optional registration fields (documented for backend, can extend later)
    val registeredCity: String? = null,
    val registeredPincode: String? = null,
    val registeredDistrict: String? = null,
    val registeredState: String? = null,
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
    val mediaMetadata: String? = null,
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

// Report events timeline
data class ReportEvent(
    val id: String,
    val type: String? = null,      // e.g., STATUS_CHANGED, NOTE_ADDED
    val status: String? = null,    // e.g., PENDING, UNDER_REVIEW, APPROVED, REJECTED
    val message: String? = null,
    val actorName: String? = null,
    val metadata: String? = null,  // Raw JSON string; may contain { "status": "..." }
    val createdAt: String,
)

// Feedback (submit)
data class FeedbackSubmitRequest(
    val feedbackType: String, // APP_FEEDBACK, REPORT_FEEDBACK, SERVICE_FEEDBACK, FEATURE_REQUEST
    val category: String = "SUGGESTION", // UI_UX, BUG, PERFORMANCE, SUGGESTION, COMPLAINT, PRAISE
    val title: String,
    val description: String,
    val rating: Int? = null, // 1..5
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val isAnonymous: Boolean = false,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val reportId: Long? = null,
    val attachments: List<String>? = null,
    val metadata: Map<String, String>? = null,
)

// Feedback listing response
data class FeedbackListPage(
    val feedback: List<FeedbackItem> = emptyList(),
    val pagination: Pagination,
)

data class FeedbackItem(
    val id: String,
    val feedbackType: String? = null,
    val category: String? = null,
    val title: String? = null,
    val description: String? = null,
    val rating: Int? = null,
    val priority: String? = null,
    val status: String? = null,
    val isAnonymous: Boolean? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val attachments: String? = null,
    val metadata: String? = null,
    val assignedTo: String? = null,
    val resolutionNotes: String? = null,
    val resolvedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val responses: List<FeedbackResponse>? = null,
)

data class FeedbackResponse(
    val id: String,
    val feedbackId: String? = null,
    val responderId: String? = null,
    val message: String? = null,
    val isInternal: Boolean? = null,
    val createdAt: String? = null,
    val responder: Responder? = null,
)

data class Responder(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
)

// Notifications
data class NotificationItem(
    val id: String,
    val citizenId: String? = null,
    val reportId: Long? = null,
    val type: String? = null,
    val title: String? = null,
    val message: String? = null,
    val readAt: String? = null,
    val createdAt: String,
)

data class NotificationsPage(
    @SerializedName(value = "notifications", alternate = ["items"]) val notifications: List<NotificationItem> = emptyList(),
    val pagination: Pagination
)

// Rewards Transactions
data class RewardTransaction(
    val id: String,
    val citizenId: String? = null,
    val type: String, // EARN | REDEEM | ADJUST
    val reportId: Long? = null,
    val points: Int,
    val description: String? = null,
    val metadata: String? = null,
    val balanceAfter: Int? = null,
    val createdAt: String,
)

data class RewardsTransactionsPage(
    @SerializedName(value = "transactions", alternate = ["items"]) val transactions: List<RewardTransaction> = emptyList(),
    val pagination: Pagination
)


