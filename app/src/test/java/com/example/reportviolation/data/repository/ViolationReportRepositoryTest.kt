package com.example.reportviolation.data.repository

import com.example.reportviolation.data.local.dao.ViolationReportDao
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.model.SeverityLevel
import com.example.reportviolation.data.model.ReportStatus
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
    private lateinit var mockDao: ViolationReportDao
    private lateinit var mockDuplicateDetectionService: DuplicateDetectionService
    private lateinit var mockJurisdictionService: JurisdictionService
    
    @Before
    fun setup() {
        mockDao = mock()
        mockDuplicateDetectionService = mock()
        mockJurisdictionService = mock()
        repository = ViolationReportRepository(mockDao, mockDuplicateDetectionService, mockJurisdictionService)
    }
    
    @Test
    fun `insertViolationReport should call dao insertReport`() = runTest {
        // Given
        val report = createTestReport()
        whenever(mockDao.insertReport(report)).thenReturn(1L)
        
        // When
        val result = repository.insertViolationReport(report)
        
        // Then
        assertEquals(1L, result)
        verify(mockDao).insertReport(report)
    }
    
    @Test
    fun `updateViolationReport should call dao updateReport`() = runTest {
        // Given
        val report = createTestReport()
        
        // When
        repository.updateViolationReport(report)
        
        // Then
        verify(mockDao).updateReport(report)
    }
    
    @Test
    fun `deleteViolationReport should call dao deleteReport`() = runTest {
        // Given
        val report = createTestReport()
        
        // When
        repository.deleteViolationReport(report)
        
        // Then
        verify(mockDao).deleteReport(report)
    }
    
    @Test
    fun `getViolationReportById should return report from dao`() = runTest {
        // Given
        val report = createTestReport()
        whenever(mockDao.getReportById(1L)).thenReturn(report)
        
        // When
        val result = repository.getViolationReportById(1L)
        
        // Then
        assertEquals(report, result)
        verify(mockDao).getReportById(1L)
    }
    
    @Test
    fun `getViolationReportsByReporter should return flow from dao`() = runTest {
        // Given
        val reports = listOf(createTestReport())
        whenever(mockDao.getReportsByReporter("test_user")).thenReturn(flowOf(reports))
        
        // When
        val result = repository.getViolationReportsByReporter("test_user")
        
        // Then
        result.collect { collectedReports ->
            assertEquals(reports, collectedReports)
        }
        verify(mockDao).getReportsByReporter("test_user")
    }
    
    @Test
    fun `getViolationReportsByStatus should return flow from dao`() = runTest {
        // Given
        val reports = listOf(createTestReport())
        whenever(mockDao.getReportsByStatus(ReportStatus.PENDING)).thenReturn(flowOf(reports))
        
        // When
        val result = repository.getViolationReportsByStatus(ReportStatus.PENDING)
        
        // Then
        result.collect { collectedReports ->
            assertEquals(reports, collectedReports)
        }
        verify(mockDao).getReportsByStatus(ReportStatus.PENDING)
    }
    
    @Test
    fun `validateJurisdiction should return true when within jurisdiction`() = runTest {
        // Given
        val report = createTestReport()
        whenever(mockJurisdictionService.isWithinJurisdiction(any(), any())).thenReturn(true)
        
        // When
        val result = repository.validateJurisdiction(report, "Mumbai", "400001")
        
        // Then
        assertTrue(result)
        verify(mockJurisdictionService).isWithinJurisdiction("Mumbai", "Mumbai")
    }
    
    @Test
    fun `validateJurisdiction should return false when outside jurisdiction`() = runTest {
        // Given
        val report = createTestReport()
        whenever(mockJurisdictionService.isWithinJurisdiction(any(), any())).thenReturn(false)
        
        // When
        val result = repository.validateJurisdiction(report, "Delhi", "110001")
        
        // Then
        assertFalse(result)
        verify(mockJurisdictionService).isWithinJurisdiction("Mumbai", "Delhi")
    }
    
    @Test
    fun `getReportStatistics should return correct statistics`() = runTest {
        // Given
        whenever(mockDao.getReportCountByReporter("test_user")).thenReturn(10)
        whenever(mockDao.getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)).thenReturn(5)
        whenever(mockDao.getReportCountByReporterAndStatus("test_user", ReportStatus.PENDING)).thenReturn(3)
        whenever(mockDao.getReportCountByReporterAndStatus("test_user", ReportStatus.REJECTED)).thenReturn(2)
        
        // When
        val result = repository.getReportStatistics("test_user")
        
        // Then
        assertEquals(10, result["total"])
        assertEquals(5, result["approved"])
        assertEquals(3, result["pending"])
        assertEquals(2, result["rejected"])
    }
    
    @Test
    fun `getTotalReportsByReporter should return count from dao`() = runTest {
        // Given
        whenever(mockDao.getReportCountByReporter("test_user")).thenReturn(10)
        
        // When
        val result = repository.getTotalReportsByReporter("test_user")
        
        // Then
        assertEquals(10, result)
        verify(mockDao).getReportCountByReporter("test_user")
    }
    
    @Test
    fun `getReportsByStatusAndReporter should return count from dao`() = runTest {
        // Given
        whenever(mockDao.getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)).thenReturn(5)
        
        // When
        val result = repository.getReportsByStatusAndReporter("test_user", ReportStatus.APPROVED)
        
        // Then
        assertEquals(5, result)
        verify(mockDao).getReportCountByReporterAndStatus("test_user", ReportStatus.APPROVED)
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
            vehicleType = null,
            vehicleColor = null,
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
