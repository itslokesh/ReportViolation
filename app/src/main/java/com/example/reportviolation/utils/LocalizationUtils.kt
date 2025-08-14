package com.example.reportviolation.utils

import android.content.Context
import com.example.reportviolation.R
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.model.ReportStatus

// Utility functions for localization
fun getLocalizedViolationTypeName(violationType: ViolationType, context: Context): String {
    return when (violationType) {
        ViolationType.WRONG_SIDE_DRIVING -> context.getString(R.string.wrong_side_driving)
        ViolationType.NO_PARKING_ZONE -> context.getString(R.string.no_parking_zone)
        ViolationType.SIGNAL_JUMPING -> context.getString(R.string.signal_violation)
        ViolationType.SPEED_VIOLATION -> context.getString(R.string.overspeeding)
        ViolationType.HELMET_SEATBELT_VIOLATION -> context.getString(R.string.helmet_seatbelt_violation)
        ViolationType.MOBILE_PHONE_USAGE -> context.getString(R.string.mobile_phone_usage)
        ViolationType.LANE_CUTTING -> context.getString(R.string.lane_cutting)
        ViolationType.DRUNK_DRIVING_SUSPECTED -> context.getString(R.string.drunk_driving_suspected)
        ViolationType.OTHERS -> context.getString(R.string.others)
    }
}

fun getLocalizedStatusName(status: ReportStatus, context: Context): String {
    return when (status) {
        ReportStatus.PENDING -> context.getString(R.string.status_submitted)
        ReportStatus.UNDER_REVIEW -> context.getString(R.string.status_in_progress)
        ReportStatus.APPROVED -> context.getString(R.string.status_resolved)
        ReportStatus.REJECTED -> context.getString(R.string.status_rejected)
        ReportStatus.DUPLICATE -> context.getString(R.string.status_rejected) // Using rejected for duplicate
    }
}

