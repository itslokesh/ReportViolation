package com.example.reportviolation.data.repository

import com.example.reportviolation.data.local.dao.ViolationReportDao
import com.example.reportviolation.data.model.*
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.time.LocalDateTime

class ViolationReportRepositoryTest {
    
    private lateinit var repository: ViolationReportRepository
    private lateinit var violationReportDao: ViolationReportDao
    private lateinit var duplicateDetectionService: DuplicateDetectionService
    private lateinit var jurisdictionService: JurisdictionService
    
    @Before
    fun setup() {
        violationReportDao = mock()
        duplicateDetectionService = mock()
        jurisdictionService = mock()
        repository = ViolationReportRepository(
            violationReportDao,
            duplicateDetectionService,
            jurisdictionService
        )
    }
    
    @Test
    fun `test insertViolationReport`() = runTest {
        // Given
        val report = createTestReport()
        whenever(violationReportDao.insertReport(report)).thenReturn(1L)
        
        // When
        val result = repository.insertViolationReport(report)
        
        // Then
        assertEquals(1L, result)
        verify(violationReportDao).insertReport(report)
    }
    
    @Test
    fun `test updateViolationReport`() = runTest {
        // Given
        val report = createTestReport()
        
        // When
        repository.updateViolationReport(report)
        
        // Then
        verify(violationReportDao).updateReport(report)
    }
    
    @Test
    fun `test deleteViolationReport`() = runTest {
        // Given
        val report = createTestReport()
        
        // When
        repository.deleteViolationReport(report)
        
        // Then
        verify(violationReportDao).deleteReport(report)
    }
    
    @Test
    fun `test getViolationReportById`() = runTest {
        // Given
        val report = createTestReport()
        whenever(violationReportDao.getReportById(1L)).thenReturn(report)
        
        // When
        val result = repository.getViolationReportById(1L)
        
        // Then
        assertEquals(report, result)
        verify(violationReportDao).getReportById(1L)
    }
    
    @Test
    fun `test getViolationReportsByReporter`() = runTest {
        // Given
        val reports = listOf(createTestReport(), createTestReport(id = 2L))
        whenever(violationReportDao.getReportsByReporter("test_user")).thenReturn(flowOf(reports))
        
        // When
        val result = repository.getViolationReportsByReporter("test_user")
        
        // Then
        result.collect { collectedReports ->
            assertEquals(reports, collectedReports)
        }
        verify(violationReportDao).getReportsByReporter("test_user")
    }
    
    @Test
    fun `test getViolationReportsByStatus`() = runTest {
        // Given
        val reports = listOf(createTestReport())
        whenever(violationReportDao.getReportsByStatus(ReportStatus.PENDING)).thenReturn(flowOf(reports))
        
        // When
        val result = repository.getViolationReportsByStatus(ReportStatus.PENDING)
        
        // Then
        result.collect { collectedReports ->
            assertEquals(reports, collectedReports)
        }
        verify(violationReportDao).getReportsByStatus(ReportStatus.PENDING)
    }
    
    @Test
    fun `test processViolationReport with duplicate detection`() = runTest {
        // Given
        val report = createTestReport()
        val existingReports = listOf(createTestReport(id = 2L))
        whenever(violationReportDao.findPotentialDuplicates(
            any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(existingReports)
        whenever(duplicateDetectionService.areLikelyDuplicates(any(), any(), any())).thenReturn(true)
        
        // When
        val result = repository.processViolationReport(report)
        
        // Then
        assertEquals(ReportStatus.DUPLICATE, result.status)
        assertTrue(result.reviewNotes?.contains("Auto-detected as duplicate") == true)
        verify(violationReportDao).findPotentialDuplicates(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
        verify(duplicateDetectionService).areLikelyDuplicates(any(), any(), any())
    }
    
    @Test
    fun `test processViolationReport without duplicate detection`() = runTest {
        // Given
        val report = createTestReport()
        whenever(violationReportDao.findPotentialDuplicates(
            any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(emptyList())
        
        // When
        val result = repository.processViolationReport(report)
        
        // Then
        assertEquals(report, result)
        verify(violationReportDao).findPotentialDuplicates(
            any(), any(), any(), any(), any(), any(), any(), any()
        )
    }
    
    @Test
    fun `test validateJurisdiction`() = runTest {
        // Given
        val report = createTestReport()
        whenever(jurisdictionService.isWithinJurisdiction(any(), any())).thenReturn(true)
        
        // When
        val result = repository.validateJurisdiction(report, "Mumbai", "400001")
        
        // Then
        assertTrue(result)
        verify(jurisdictionService).isWithinJurisdiction("Mumbai", "Mumbai")
    }
    
    @Test
    fun `test getReportStatistics`() = runTest {
        // Given
        whenever(violationReportDao.getReportCountByReporter("test_user")).thenReturn(10)
        whenever(violationReportDao.getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)).thenReturn(5)
        whenever(violationReportDao.getReportCountByReporterAndStatus("test_user", ReportStatus.PENDING)).thenReturn(3)
        whenever(violationReportDao.getReportCountByReporterAndStatus("test_user", ReportStatus.REJECTED)).thenReturn(2)
        
        // When
        val result = repository.getReportStatistics("test_user")
        
        // Then
        assertEquals(10, result["total"])
        assertEquals(5, result["approved"])
        assertEquals(3, result["pending"])
        assertEquals(2, result["rejected"])
    }
    
    @Test
    fun `test getTotalReportsByReporter`() = runTest {
        // Given
        whenever(violationReportDao.getReportCountByReporter("test_user")).thenReturn(10)
        
        // When
        val result = repository.getTotalReportsByReporter("test_user")
        
        // Then
        assertEquals(10, result)
        verify(violationReportDao).getReportCountByReporter("test_user")
    }
    
    @Test
    fun `test getReportsByStatusAndReporter`() = runTest {
        // Given
        whenever(violationReportDao.getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)).thenReturn(5)
        
        // When
        val result = repository.getReportsByStatusAndReporter("test_user", ReportStatus.APPROVED)
        
        // Then
        assertEquals(5, result)
        verify(violationReportDao).getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)
    }
    
    @Test
    fun `test markAsDuplicate`() = runTest {
        // Given
        val reportId = 1L
        val duplicateOfId = 2L
        
        // When
        repository.markAsDuplicate(reportId, duplicateOfId)
        
        // Then
        verify(violationReportDao).updateReportStatus(reportId, ReportStatus.DUPLICATE)
        verify(violationReportDao).updateReportStatus(duplicateOfId, ReportStatus.APPROVED)
    }
    
    private fun createTestReport(id: Long = 1L): ViolationReport {
        return ViolationReport(
            id = id,
            reporterId = "test_user",
            reporterPhone = "1234567890",
            reporterCity = "Mumbai",
            reporterPincode = "400001",
            violationType = ViolationType.SIGNAL_JUMPING,
            severity = SeverityLevel.MINOR,
            description = "Test violation",
            timestamp = LocalDateTime.now(),
            latitude = 19.0760,
            longitude = 72.8777,
            address = "Test Address",
            pincode = "400001",
            city = "Mumbai",
            district = "Mumbai",
            state = "Maharashtra",
            vehicleNumber = "MH12AB1234",
            vehicleType = VehicleType.FOUR_WHEELER,
            vehicleColor = "White",
            photoUri = "test_photo_uri",
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
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isAnonymous = false
        )
    }
}
