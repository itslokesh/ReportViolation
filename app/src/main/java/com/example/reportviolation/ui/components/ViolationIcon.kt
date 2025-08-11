package com.example.reportviolation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reportviolation.R
import com.example.reportviolation.data.model.ViolationType

@Composable
fun ViolationIcon(
    violationType: ViolationType,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    displayMode: ViolationIconDisplayMode = ViolationIconDisplayMode.SELECTION
) {
    when (displayMode) {
        ViolationIconDisplayMode.SELECTION -> SelectionIcon(
            violationType = violationType,
            isSelected = isSelected,
            modifier = modifier
        )
        ViolationIconDisplayMode.REPORT_DETAILS -> ReportDetailsIcon(
            violationType = violationType,
            modifier = modifier
        )
        ViolationIconDisplayMode.QUICK_SELECTION -> QuickSelectionIcon(
            violationType = violationType,
            modifier = modifier
        )
    }
}

@Composable
private fun SelectionIcon(
    violationType: ViolationType,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF1976D2) else Color(0xFFF5F5F5)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = getViolationIconRes(violationType)),
            contentDescription = violationType.name,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
private fun ReportDetailsIcon(
    violationType: ViolationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(getViolationTypeColor(violationType)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = getViolationIconRes(violationType)),
            contentDescription = violationType.name,
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun QuickSelectionIcon(
    violationType: ViolationType,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = getViolationIconRes(violationType)),
        contentDescription = violationType.name,
        modifier = modifier.size(20.dp),
        tint = Color(0xFF1976D2)
    )
}

enum class ViolationIconDisplayMode {
    SELECTION,      // 48dp touch target with background
    REPORT_DETAILS, // 32dp icon in 40dp circle with violation color
    QUICK_SELECTION // 20dp icon, no background, primary blue
}

private fun getViolationIconRes(violationType: ViolationType): Int {
    return when (violationType) {
        ViolationType.SPEED_VIOLATION -> R.drawable.ic_speed_violation
        ViolationType.SIGNAL_JUMPING -> R.drawable.ic_signal_jumping
        ViolationType.WRONG_SIDE_DRIVING -> R.drawable.ic_wrong_side_driving
        ViolationType.DRUNK_DRIVING_SUSPECTED -> R.drawable.ic_drunk_driving_suspected
        ViolationType.LANE_CUTTING -> R.drawable.ic_lane_cutting
        ViolationType.MOBILE_PHONE_USAGE -> R.drawable.ic_mobile_phone_usage
        ViolationType.HELMET_SEATBELT_VIOLATION -> R.drawable.ic_helmet_seatbelt_violation
        ViolationType.NO_PARKING_ZONE -> R.drawable.ic_no_parking_zone
        ViolationType.OTHERS -> R.drawable.ic_others
    }
}

private fun getViolationTypeColor(violationType: ViolationType): Color {
    return when (violationType) {
        ViolationType.SPEED_VIOLATION -> Color(0xFFE53935)      // Red
        ViolationType.SIGNAL_JUMPING -> Color(0xFFFF9800)      // Orange
        ViolationType.WRONG_SIDE_DRIVING -> Color(0xFFF44336)  // Red
        ViolationType.DRUNK_DRIVING_SUSPECTED -> Color(0xFF8E24AA) // Purple
        ViolationType.LANE_CUTTING -> Color(0xFFFF5722)        // Deep Orange
        ViolationType.MOBILE_PHONE_USAGE -> Color(0xFF795548)  // Brown
        ViolationType.HELMET_SEATBELT_VIOLATION -> Color(0xFF607D8B) // Blue Grey
        ViolationType.NO_PARKING_ZONE -> Color(0xFF9C27B0)     // Purple
        ViolationType.OTHERS -> Color(0xFF757575)              // Grey
    }
}
