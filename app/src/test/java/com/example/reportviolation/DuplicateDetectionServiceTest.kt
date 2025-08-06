package com.example.reportviolation

import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.model.SeverityLevel
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.domain.service.DuplicateDetectionService
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class DuplicateDetectionServiceTest {
    
    private val duplicateDetectionService = DuplicateDetectionService()
    
    @Test
    fun `test duplicate detection with same location and time`() {
        val baseTime = LocalDateTime.now()
        val baseLat = 19.0760
        val baseLng = 72.8777
        
        val report1 = createTestReport(
            id = 1L,
            latitude = baseLat,
            longitude = baseLng,
            timestamp = baseTime,
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val report2 = createTestReport(
            id = 2L,
            latitude = baseLat + 0.0001, // Very close location
            longitude = baseLng + 0.0001,
            timestamp = baseTime.plusMinutes(5), // Within 30 minutes
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val confidenceScore = duplicateDetectionService.calculateConfidenceScore(report1, listOf(report2))
        
        assertTrue("Confidence score should be high for similar reports", confidenceScore > 0.7)
    }
    
    @Test
    fun `test duplicate detection with different locations`() {
        val baseTime = LocalDateTime.now()
        
        val report1 = createTestReport(
            id = 1L,
            latitude = 19.0760,
            longitude = 72.8777,
            timestamp = baseTime,
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val report2 = createTestReport(
            id = 2L,
            latitude = 19.0860, // Far location
            longitude = 72.8877,
            timestamp = baseTime.plusMinutes(5),
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val confidenceScore = duplicateDetectionService.calculateConfidenceScore(report1, listOf(report2))
        
        assertTrue("Confidence score should be low for distant reports", confidenceScore < 0.5)
    }
    
    @Test
    fun `test duplicate detection with different times`() {
        val baseTime = LocalDateTime.now()
        val baseLat = 19.0760
        val baseLng = 72.8777
        
        val report1 = createTestReport(
            id = 1L,
            latitude = baseLat,
            longitude = baseLng,
            timestamp = baseTime,
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val report2 = createTestReport(
            id = 2L,
            latitude = baseLat,
            longitude = baseLng,
            timestamp = baseTime.plusHours(2), // 2 hours later
            violationType = ViolationType.SIGNAL_JUMPING
        )
        
        val confidenceScore = duplicateDetectionService.calculateConfidenceScore(report1, listOf(report2))
        
        assertTrue("Confidence score should be low for reports with large time gap", confidenceScore < 0.5)
    }
    
    @Test
    fun `test duplicate detection with same vehicle number`() {
        val baseTime = LocalDateTime.now()
        
        val report1 = createTestReport(
            id = 1L,
            latitude = 19.0760,
            longitude = 72.8777,
            timestamp = baseTime,
            violationType = ViolationType.SIGNAL_JUMPING,
            vehicleNumber = "MH12AB1234"
        )
        
        val report2 = createTestReport(
            id = 2L,
            latitude = 19.0860, // Different location
            longitude = 72.8877,
            timestamp = baseTime.plusMinutes(30),
            violationType = ViolationType.SIGNAL_JUMPING,
            vehicleNumber = "MH12AB1234" // Same vehicle
        )
        
        val confidenceScore = duplicateDetectionService.calculateConfidenceScore(report1, listOf(report2))
        
        assertTrue("Confidence score should be higher for same vehicle", confidenceScore > 0.3)
    }
    
    private fun createTestReport(
        id: Long,
        latitude: Double,
        longitude: Double,
        timestamp: LocalDateTime,
        violationType: ViolationType,
        vehicleNumber: String? = null
    ): ViolationReport {
        return ViolationReport(
            id = id,
            reporterId = "test_user",
            reporterPhone = "1234567890",
            reporterCity = "Mumbai",
            reporterPincode = "400001",
            violationType = violationType,
            severity = SeverityLevel.MINOR,
            description = "Test violation",
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            address = "Test Address",
            pincode = "400001",
            city = "Mumbai",
            district = "Mumbai",
            state = "Maharashtra",
            vehicleNumber = vehicleNumber,
            vehicleType = null,
            vehicleColor = null,
            photoUri = null,
            videoUri = null,
            mediaMetadata = null,
            status = ReportStatus.PENDING,
            isDuplicate = false,
            duplicateGroupId = null,
            confidenceScore = null,
            reviewerId = null,
            reviewTimestamp = null,
            reviewNotes = null,
            challanIssued = false,
            challanNumber = null,
            pointsAwarded = 0,
            isFirstReporter = false,
            createdAt = timestamp,
            updatedAt = timestamp,
            isAnonymous = false
        )
    }
} 