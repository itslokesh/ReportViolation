package com.example.reportviolation.domain.service

class JurisdictionService() {
    
    /**
     * Validates if a user can report violations in a specific city
     */
    fun isWithinJurisdiction(reportCity: String, userCity: String): Boolean {
        return reportCity.equals(userCity, ignoreCase = true)
    }
    
    /**
     * Validates if a user can report violations in a specific pincode
     */
    fun isWithinPincodeJurisdiction(reportPincode: String, userPincode: String): Boolean {
        // For now, exact match. Can be extended to include nearby pincodes
        return reportPincode == userPincode
    }
    
    /**
     * Gets the police department for a given city
     */
    fun getPoliceDepartment(city: String): String {
        return "Traffic Police Department - $city"
    }
    
    /**
     * Validates if a user has multi-city access
     */
    fun hasMultiCityAccess(userAuthorizedCities: List<String>, targetCity: String): Boolean {
        return userAuthorizedCities.any { it.equals(targetCity, ignoreCase = true) }
    }
} 