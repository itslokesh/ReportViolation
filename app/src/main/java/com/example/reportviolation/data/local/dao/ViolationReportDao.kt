package com.example.reportviolation.data.local.dao

import androidx.room.*
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.data.model.ViolationType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ViolationReportDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ViolationReport): Long
    
    @Update
    suspend fun updateReport(report: ViolationReport)
    
    @Delete
    suspend fun deleteReport(report: ViolationReport)
    
    @Query("SELECT * FROM violation_reports WHERE id = :reportId")
    suspend fun getReportById(reportId: Long): ViolationReport?
    
    @Query("SELECT * FROM violation_reports ORDER BY createdAt DESC")
    suspend fun getAllReports(): List<ViolationReport>
    
    @Query("SELECT * FROM violation_reports WHERE reporterId = :reporterId ORDER BY createdAt DESC")
    fun getReportsByReporter(reporterId: String): Flow<List<ViolationReport>>
    
    @Query("SELECT * FROM violation_reports WHERE city = :city ORDER BY createdAt DESC")
    fun getReportsByCity(city: String): Flow<List<ViolationReport>>
    
    @Query("SELECT * FROM violation_reports WHERE pincode = :pincode ORDER BY createdAt DESC")
    fun getReportsByPincode(pincode: String): Flow<List<ViolationReport>>
    
    @Query("SELECT * FROM violation_reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: ReportStatus): Flow<List<ViolationReport>>
    
    @Query("SELECT * FROM violation_reports WHERE violationType = :violationType ORDER BY createdAt DESC")
    fun getReportsByViolationType(violationType: ViolationType): Flow<List<ViolationReport>>
    
    // Duplicate Detection Queries
    @Query("""
        SELECT * FROM violation_reports 
        WHERE latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLng AND :maxLng 
        AND timestamp BETWEEN :startTime AND :endTime
        AND violationType = :violationType
        AND id != :excludeId
        ORDER BY createdAt ASC
    """)
    suspend fun findPotentialDuplicates(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        violationType: ViolationType,
        excludeId: Long = 0
    ): List<ViolationReport>
    
    @Query("""
        SELECT * FROM violation_reports 
        WHERE vehicleNumber = :vehicleNumber 
        AND timestamp BETWEEN :startTime AND :endTime
        AND violationType = :violationType
        AND id != :excludeId
        ORDER BY createdAt ASC
    """)
    suspend fun findDuplicatesByVehicle(
        vehicleNumber: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        violationType: ViolationType,
        excludeId: Long = 0
    ): List<ViolationReport>
    
    // Jurisdiction-based Queries
    @Query("""
        SELECT * FROM violation_reports 
        WHERE city = :city 
        AND status = :status 
        ORDER BY 
        CASE 
            WHEN severity = 'CRITICAL' THEN 1
            WHEN severity = 'MAJOR' THEN 2
            WHEN severity = 'MINOR' THEN 3
        END,
        createdAt DESC
    """)
    fun getReportsForReview(city: String, status: ReportStatus = ReportStatus.PENDING): Flow<List<ViolationReport>>
    
    // Statistics Queries
    @Query("SELECT COUNT(*) FROM violation_reports WHERE reporterId = :reporterId")
    suspend fun getReportCountByReporter(reporterId: String): Int
    
    @Query("SELECT COUNT(*) FROM violation_reports WHERE reporterId = :reporterId AND status = :status")
    suspend fun getReportCountByReporterAndStatus(reporterId: String, status: ReportStatus): Int
    
    @Query("SELECT COUNT(*) FROM violation_reports WHERE city = :city AND status = :status")
    suspend fun getReportCountByCityAndStatus(city: String, status: ReportStatus): Int
    
    // Offline Storage Queries
    @Query("SELECT * FROM violation_reports WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT 50")
    suspend fun getPendingReportsForSync(): List<ViolationReport>
    
    @Query("UPDATE violation_reports SET status = :status, updatedAt = :timestamp WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: Long, status: ReportStatus, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE violation_reports SET isDuplicate = :isDuplicate, duplicateGroupId = :groupId, confidenceScore = :score WHERE id = :reportId")
    suspend fun updateDuplicateInfo(reportId: Long, isDuplicate: Boolean, groupId: String?, score: Double?)
    
    // Cleanup Queries
    @Query("DELETE FROM violation_reports WHERE createdAt < :cutoffDate")
    suspend fun deleteOldReports(cutoffDate: LocalDateTime)
} 