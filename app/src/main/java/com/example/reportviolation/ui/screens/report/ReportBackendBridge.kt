package com.example.reportviolation.ui.screens.report

import android.content.Context
import android.net.Uri
import com.example.reportviolation.data.remote.ReportCreateBody
import com.example.reportviolation.ui.screens.auth.OtpNetworkBridge
import java.time.Instant

object ReportBackendBridge {
    suspend fun uploadMediaIfNeeded(context: Context, mediaUri: String): Pair<String?, String?> {
        return if (mediaUri.endsWith(".mp4") || mediaUri.contains("video")) {
            val url = OtpNetworkBridge.uploadVideo(context, Uri.parse(mediaUri))
            null to url
        } else {
            val url = OtpNetworkBridge.uploadPhoto(context, Uri.parse(mediaUri))
            url to null
        }
    }

    suspend fun createReport(
        violationTypes: List<String>,
        severity: String?,
        description: String?,
        latitude: Double,
        longitude: Double,
        address: String,
        pincode: String,
        city: String,
        district: String,
        state: String,
        isAnonymous: Boolean,
        vehicleNumber: String?,
        vehicleType: String?,
        vehicleColor: String?,
    ): Boolean {
        val body = ReportCreateBody(
            violationTypes = violationTypes,
            severity = severity,
            description = description,
            timestamp = com.example.reportviolation.utils.DateTimeUtils.nowIsoIst(),
            latitude = latitude,
            longitude = longitude,
            address = address,
            pincode = pincode,
            city = city,
            district = district,
            state = state,
            isAnonymous = isAnonymous,
            vehicleNumber = vehicleNumber,
            vehicleType = vehicleType,
            vehicleColor = vehicleColor,
        )
        println("API_CREATE_REPORT payload=" + com.google.gson.Gson().toJson(body))
        return OtpNetworkBridge.submitReport(body)
    }
}


