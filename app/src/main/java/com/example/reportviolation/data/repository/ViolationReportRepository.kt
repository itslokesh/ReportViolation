package com.example.reportviolation.data.repository

import com.example.reportviolation.data.local.dao.ViolationReportDao
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ViolationReportRepository(
    private val violationReportDao: ViolationReportDao,
    private val duplicateDetectionService: DuplicateDetectionService,
    private val jurisdictionService: JurisdictionService
) {
    
    // Basic CRUD operations
    suspend fun insertViolationReport(report: ViolationReport): Long {
        return violationReportDao.insertReport(report)
    }
    
    suspend fun createReport(report: ViolationReport): Long {
        return violationReportDao.insertReport(report)
    }
    
    suspend fun getAllReports(): List<ViolationReport> {
        return violationReportDao.getAllReports()
    }
    
    suspend fun updateViolationReport(report: ViolationReport) {
        violationReportDao.updateReport(report)
    }
    
    suspend fun deleteViolationReport(report: ViolationReport) {
        violationReportDao.deleteReport(report)
    }
    
    suspend fun getViolationReportById(id: Long): ViolationReport? {
        return violationReportDao.getReportById(id)
    }
    
    // Flow operations
    fun getViolationReportsByReporter(reporterId: String): Flow<List<ViolationReport>> {
        return violationReportDao.getReportsByReporter(reporterId)
    }
    
    fun getViolationReportsByStatus(status: ReportStatus): Flow<List<ViolationReport>> {
        return violationReportDao.getReportsByStatus(status)
    }
    
    // Duplicate detection
    suspend fun processViolationReport(report: ViolationReport): ViolationReport {
        // Check for duplicates using the DAO's findPotentialDuplicates method
        val existingReports = violationReportDao.findPotentialDuplicates(
            minLat = report.latitude - 0.001, // ~100m radius
            maxLat = report.latitude + 0.001,
            minLng = report.longitude - 0.001,
            maxLng = report.longitude + 0.001,
            startTime = report.timestamp.minusMinutes(30),
            endTime = report.timestamp.plusMinutes(30),
            violationType = report.violationType,
            excludeId = report.id
        )
        
        // Check for duplicates by comparing with existing reports
        val isDuplicate = existingReports.any { existingReport ->
            duplicateDetectionService.areLikelyDuplicates(report, existingReport, threshold = 0.7)
        }
        
        // Mark as duplicate if found
        val processedReport = if (isDuplicate) {
            report.copy(
                status = ReportStatus.DUPLICATE,
                reviewNotes = "Auto-detected as duplicate"
            )
        } else {
            report
        }
        
        return processedReport
    }
    
    // Jurisdiction validation
    suspend fun validateJurisdiction(report: ViolationReport, userCity: String, userPincode: String): Boolean {
        return jurisdictionService.isWithinJurisdiction(
            reportCity = report.city,
            userCity = userCity
        )
    }
    
    // Statistics
    suspend fun getReportStatistics(reporterId: String): Map<String, Int> {
        val totalReports = violationReportDao.getReportCountByReporter(reporterId)
        val approvedReports = violationReportDao.getReportCountByReporterAndStatus(reporterId, ReportStatus.APPROVED)
        val pendingReports = violationReportDao.getReportCountByReporterAndStatus(reporterId, ReportStatus.PENDING)
        val rejectedReports = violationReportDao.getReportCountByReporterAndStatus(reporterId, ReportStatus.REJECTED)
        
        return mapOf(
            "total" to totalReports,
            "approved" to approvedReports,
            "pending" to pendingReports,
            "rejected" to rejectedReports
        )
    }
    
    suspend fun getTotalReportsByReporter(reporterId: String): Int {
        return violationReportDao.getReportCountByReporter(reporterId)
    }
    
    suspend fun getLatestReportsByReporter(reporterId: String, limit: Int): List<ViolationReport> {
        return violationReportDao.getLatestReportsByReporter(reporterId, limit)
    }
    
    suspend fun getReportsByStatusAndReporter(reporterId: String, status: ReportStatus): Int {
        return violationReportDao.getReportCountByReporterAndStatus(reporterId, status)
    }
    
    // Duplicate management
    suspend fun markAsDuplicate(reportId: Long, duplicateOfId: Long) {
        violationReportDao.updateReportStatus(
            reportId,
            ReportStatus.DUPLICATE,
            java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime()
        )
        violationReportDao.updateReportStatus(
            duplicateOfId,
            ReportStatus.APPROVED,
            java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime()
        )
    }
} 