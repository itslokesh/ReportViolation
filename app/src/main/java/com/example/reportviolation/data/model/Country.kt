package com.example.reportviolation.data.model

data class Country(
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String = "",
    val phoneNumberLength: Int = 10 // Default length, will be overridden for specific countries
) {
    override fun toString(): String {
        return "$name (+$dialCode)"
    }
}
