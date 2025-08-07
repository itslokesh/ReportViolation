package com.example.reportviolation.domain.service

import com.example.reportviolation.data.model.ViolationReport
import kotlin.math.abs

class DuplicateDetectionService() {
    
    companion object {
        private const val LOCATION_THRESHOLD_METERS = 50.0
        private const val TIME_THRESHOLD_MINUTES = 30L
        private const val VEHICLE_SIMILARITY_THRESHOLD = 0.8
    }
    
    /**
     * Calculate confidence score for duplicate detection
     * Returns a value between 0.0 and 1.0, where 1.0 indicates high confidence of being a duplicate
     */
    fun calculateDuplicateConfidence(report1: ViolationReport, report2: ViolationReport): Double {
        return calculateSimilarityScore(report1, report2)
    }
    
    /**
     * Check if two reports are likely duplicates based on confidence threshold
     */
    fun areLikelyDuplicates(report1: ViolationReport, report2: ViolationReport, threshold: Double = 0.7): Boolean {
        return calculateDuplicateConfidence(report1, report2) >= threshold
    }
    
    private fun calculateSimilarityScore(report1: ViolationReport, report2: ViolationReport): Double {
        val locationScore = calculateLocationSimilarity(report1, report2)
        val timeScore = calculateTimeSimilarity(report1, report2)
        val vehicleScore = calculateVehicleSimilarity(report1, report2)
        val violationTypeScore = if (report1.violationType == report2.violationType) 1.0 else 0.0

        // Adjusted weights
        val weightedScore = (locationScore * 0.4) +
                           (timeScore * 0.4) +
                           (vehicleScore * 0.15) +
                           (violationTypeScore * 0.05)

        return weightedScore.coerceIn(0.0, 1.0)
    }
    
    private fun calculateLocationSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        val distance = calculateDistance(
            report1.latitude, report1.longitude,
            report2.latitude, report2.longitude
        )
        return when {
            distance <= LOCATION_THRESHOLD_METERS -> 1.0
            distance <= LOCATION_THRESHOLD_METERS * 2 -> 0.6 // Adjusted
            distance <= LOCATION_THRESHOLD_METERS * 3 -> 0.2 // Adjusted
            distance <= LOCATION_THRESHOLD_METERS * 5 -> 0.0 // Adjusted
            else -> 0.0
        }
    }
    
    private fun calculateTimeSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        val timeDiffMinutes = abs(
            java.time.Duration.between(report1.timestamp, report2.timestamp).toMinutes()
        )
        return when {
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES -> 1.0
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES * 2 -> 0.1 // Adjusted
            timeDiffMinutes <= TIME_THRESHOLD_MINUTES * 3 -> 0.0 // Adjusted
            else -> 0.0
        }
    }
    
    private fun calculateVehicleSimilarity(report1: ViolationReport, report2: ViolationReport): Double {
        val vehicle1 = report1.vehicleNumber?.trim()?.uppercase() ?: ""
        val vehicle2 = report2.vehicleNumber?.trim()?.uppercase() ?: ""
        
        if (vehicle1.isEmpty() || vehicle2.isEmpty()) return 0.0
        if (vehicle1 == vehicle2) return 1.0
        
        return calculateLevenshteinSimilarity(vehicle1, vehicle2)
    }
    
    private fun calculateLevenshteinSimilarity(str1: String, str2: String): Double {
        val distance = calculateLevenshteinDistance(str1, str2)
        val maxLength = maxOf(str1.length, str2.length)
        return if (maxLength == 0) 1.0 else (maxLength - distance).toDouble() / maxLength
    }
    
    private fun calculateLevenshteinDistance(str1: String, str2: String): Int {
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
    
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLng = Math.toRadians(lng2 - lng1)
        
        val a = kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLng / 2) * kotlin.math.sin(deltaLng / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
}
