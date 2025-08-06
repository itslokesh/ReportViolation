package com.example.reportviolation.domain.service

import com.example.reportviolation.data.model.ViolationReport
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2

class DuplicateDetectionService() {
    
    companion object {
        private const val LOCATION_THRESHOLD_METERS = 50.0
        private const val TIME_THRESHOLD_MINUTES = 30
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.8
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.6
    }
    
    /**
     * Calculates confidence score for duplicate detection
     * @param report The current report
     * @param potentialDuplicates List of potential duplicate reports
     * @return Confidence score between 0.0 and 1.0
     */
    fun calculateConfidenceScore(report: ViolationReport, potentialDuplicates: List<ViolationReport>): Double {
        if (potentialDuplicates.isEmpty()) return 0.0
        
        val scores = potentialDuplicates.map { duplicate ->
            calculateSimilarityScore(report, duplicate)
        }
        
        return scores.maxOrNull() ?: 0.0
    }
    
    /**
     * Calculates similarity score between two reports
     */
    private fun calculateSimilarityScore(report1: ViolationReport, report2: ViolationReport): Double {
        val locationScore = calculateLocationSimilarity(report1, report2)
        val timeScore = calculateTimeSimilarity(report1, report2)
        val vehicleScore = calculateVehicleSimilarity(report1, report2)
        val violationTypeScore = if (report1.violationType == report2.violationType) 1.0 else 0.0
        
        // Weighted average of all similarity factors
        val weightedScore = (locationScore * 0.4) + 
                           (timeScore * 0.3) + 
                           (vehicleScore * 0.2) + 
                           (violationTypeScore * 0.1)
        
        return weightedScore.coerceIn(0.0, 1.0)
    }
    
    /**
     * Calculates location similarity based on distance
     */
    private fun calculateLocationSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        val distance = calculateDistance(
            report1.latitude, report1.longitude,
            report2.latitude, report2.longitude
        )
        
        return when {
            distance <= LOCATION_THRESHOLD_METERS -> 1.0
            distance <= LOCATION_THRESHOLD_METERS * 2 -> 0.8
            distance <= LOCATION_THRESHOLD_METERS * 3 -> 0.6
            distance <= LOCATION_THRESHOLD_METERS * 5 -> 0.4
            else -> 0.0
        }
    }
    
    /**
     * Calculates time similarity based on time difference
     */
    private fun calculateTimeSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        val timeDiffMinutes = abs(
            java.time.Duration.between(report1.timestamp, report2.timestamp).toMinutes()
        )
        
        return when {
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES -> 1.0
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES * 2 -> 0.8
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES * 3 -> 0.6
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES * 5 -> 0.4
            else -> 0.0
        }
    }
    
    /**
     * Calculates vehicle similarity based on vehicle number and type
     */
    private fun calculateVehicleSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        var score = 0.0
        
        // Vehicle number similarity (highest weight)
        if (report1.vehicleNumber != null && report2.vehicleNumber != null) {
            val numberSimilarity = calculateStringSimilarity(
                report1.vehicleNumber.uppercase(),
                report2.vehicleNumber.uppercase()
            )
            score += numberSimilarity * 0.7
        }
        
        // Vehicle type similarity
        if (report1.vehicleType != null && report2.vehicleType != null) {
            if (report1.vehicleType == report2.vehicleType) {
                score += 0.2
            }
        }
        
        // Vehicle color similarity
        if (report1.vehicleColor != null && report2.vehicleColor != null) {
            val colorSimilarity = calculateStringSimilarity(
                report1.vehicleColor.uppercase(),
                report2.vehicleColor.uppercase()
            )
            score += colorSimilarity * 0.1
        }
        
        return score.coerceIn(0.0, 1.0)
    }
    
    /**
     * Calculates string similarity using Levenshtein distance
     */
    private fun calculateStringSimilarity(str1: String, str2: String): Double {
        val distance = levenshteinDistance(str1, str2)
        val maxLength = maxOf(str1.length, str2.length)
        return if (maxLength == 0) 1.0 else (maxLength - distance) / maxLength.toDouble()
    }
    
    /**
     * Calculates Levenshtein distance between two strings
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val matrix = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) matrix[i][0] = i
        for (j in 0..str2.length) matrix[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,      // deletion
                    matrix[i][j - 1] + 1,      // insertion
                    matrix[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return matrix[str1.length][str2.length]
    }
    
    /**
     * Calculates distance between two GPS coordinates using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth's radius in meters
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return r * c
    }
    
    /**
     * Determines if a report is likely a duplicate based on confidence score
     */
    fun isDuplicate(confidenceScore: Double): Boolean {
        return confidenceScore >= MEDIUM_CONFIDENCE_THRESHOLD
    }
    
    /**
     * Determines if a report is a high-confidence duplicate
     */
    fun isHighConfidenceDuplicate(confidenceScore: Double): Boolean {
        return confidenceScore >= HIGH_CONFIDENCE_THRESHOLD
    }
    
    /**
     * Groups reports by similarity for batch processing
     */
    fun groupSimilarReports(reports: List<ViolationReport>): List<List<ViolationReport>> {
        val groups = mutableListOf<List<ViolationReport>>()
        val processed = mutableSetOf<Long>()
        
        for (report in reports) {
            if (report.id in processed) continue
            
            val group = mutableListOf(report)
            processed.add(report.id)
            
            for (otherReport in reports) {
                if (otherReport.id in processed) continue
                
                val similarity = calculateSimilarityScore(report, otherReport)
                if (similarity >= MEDIUM_CONFIDENCE_THRESHOLD) {
                    group.add(otherReport)
                    processed.add(otherReport.id)
                }
            }
            
            if (group.size > 1) {
                groups.add(group)
            }
        }
        
        return groups
    }
} 