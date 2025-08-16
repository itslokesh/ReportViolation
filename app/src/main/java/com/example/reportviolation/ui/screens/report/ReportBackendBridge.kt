package com.example.reportviolation.ui.screens.report

import android.content.Context
import android.net.Uri
import com.example.reportviolation.data.remote.ReportCreateBody
import com.example.reportviolation.ui.screens.auth.OtpNetworkBridge

object ReportBackendBridge {
	/**
	 * Upload helper kept for completeness; two-step flow now uses these directly.
	 */
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
		context: Context,
		selectedMediaUri: String?,
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
		var photoUrl: String? = null
		var videoUrl: String? = null
		selectedMediaUri?.let { uriStr ->
			val (p, v) = uploadMediaIfNeeded(context, uriStr)
			photoUrl = p
			videoUrl = v
		}

		val body = ReportCreateBody(
			violationTypes = violationTypes,
			severity = severity,
			description = description,
			timestamp = java.time.Instant.now().toString(),
			latitude = latitude,
			longitude = longitude,
			address = address,
			pincode = pincode,
			city = city,
			district = district,
			state = state,
			vehicleNumber = vehicleNumber,
			vehicleType = vehicleType,
			vehicleColor = vehicleColor,
			isAnonymous = isAnonymous,
			photoUrl = photoUrl,
			videoUrl = videoUrl
		)

		println("API_CREATE_REPORT payload=" + com.google.gson.Gson().toJson(body) +
				" with photoUrl=" + (photoUrl ?: "-") + ", videoUrl=" + (videoUrl ?: "-"))

		// Submit using JSON path with urls embedded
		return try {
			OtpNetworkBridge.submitReport(body)
		} catch (e: Exception) {
			println("API_CREATE_REPORT error=${e.message}")
			false
		}
	}
}


