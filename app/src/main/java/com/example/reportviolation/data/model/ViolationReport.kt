package com.example.reportviolation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "violation_reports")
data class ViolationReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Reporter Information
    val reporterId: String,
    val reporterPhone: String,
    val reporterCity: String,
    val reporterPincode: String,
    
    // Violation Details
    val violationType: ViolationType,
    val severity: SeverityLevel,
    val description: String?,
    val timestamp: LocalDateTime,
    
    // Location Information
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val pincode: String,
    val city: String,
    val district: String,
    val state: String,
    
    // Vehicle Information
    val vehicleNumber: String?,
    val vehicleType: VehicleType?,
    val vehicleColor: String?,
    
    // Media Information
    val photoUri: String?,
    val videoUri: String?,
    val mediaMetadata: String?, // JSON string containing metadata
    
    // Status and Processing
    val status: ReportStatus = ReportStatus.PENDING,
    val isDuplicate: Boolean = false,
    val duplicateGroupId: String? = null,
    val confidenceScore: Double? = null,
    
    // Review Information
    val reviewerId: String? = null,
    val reviewTimestamp: LocalDateTime? = null,
    val reviewNotes: String? = null,
    val challanIssued: Boolean = false,
    val challanNumber: String? = null,
    
    // Reward Information
    val pointsAwarded: Int = 0,
    val isFirstReporter: Boolean = false,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isAnonymous: Boolean = false
)

enum class ViolationType {
    WRONG_SIDE_DRIVING,
    NO_PARKING_ZONE,
    SIGNAL_JUMPING,
    SPEED_VIOLATION,
    HELMET_SEATBELT_VIOLATION,
    MOBILE_PHONE_USAGE,
    LANE_CUTTING,
    DRUNK_DRIVING_SUSPECTED,
    OTHERS
}

enum class SeverityLevel {
    MINOR,
    MAJOR,
    CRITICAL
}

enum class VehicleType {
    TWO_WHEELER,
    FOUR_WHEELER,
    COMMERCIAL_VEHICLE,
    HEAVY_VEHICLE,
    AUTO_RICKSHAW,
    BUS,
    TRUCK,
    OTHERS
}

enum class ReportStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    DUPLICATE
} 