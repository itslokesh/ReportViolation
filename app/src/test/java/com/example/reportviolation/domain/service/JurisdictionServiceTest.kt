package com.example.reportviolation.domain.service

import org.junit.Test
import org.junit.Assert.*

class JurisdictionServiceTest {
    
    private val jurisdictionService = JurisdictionService()
    
    @Test
    fun `isWithinJurisdiction should return true for same city`() {
        // Given
        val reportCity = "Mumbai"
        val userCity = "Mumbai"
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertTrue("Should be within jurisdiction for same city", result)
    }
    
    @Test
    fun `isWithinJurisdiction should return false for different cities`() {
        // Given
        val reportCity = "Mumbai"
        val userCity = "Delhi"
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertFalse("Should not be within jurisdiction for different cities", result)
    }
    
    @Test
    fun `isWithinJurisdiction should return true for case insensitive comparison`() {
        // Given
        val reportCity = "Mumbai"
        val userCity = "mumbai"
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertTrue("Should be within jurisdiction for case insensitive comparison", result)
    }
    
    @Test
    fun `isWithinJurisdiction should return false for trimmed city names`() {
        // Given
        val reportCity = "Mumbai"
        val userCity = "  Mumbai  "
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertFalse("Should not be within jurisdiction for trimmed city names", result)
    }
    
    @Test
    fun `isWithinJurisdiction should return false for empty user city`() {
        // Given
        val reportCity = "Mumbai"
        val userCity = ""
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertFalse("Should not be within jurisdiction for empty user city", result)
    }
    
    @Test
    fun `isWithinJurisdiction should handle special characters`() {
        // Given
        val reportCity = "New York"
        val userCity = "New York"
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertTrue("Should be within jurisdiction for cities with special characters", result)
    }
    
    @Test
    fun `isWithinJurisdiction should handle numbers in city names`() {
        // Given
        val reportCity = "Mumbai 1"
        val userCity = "Mumbai 1"
        
        // When
        val result = jurisdictionService.isWithinJurisdiction(reportCity, userCity)
        
        // Then
        assertTrue("Should be within jurisdiction for cities with numbers", result)
    }
}
