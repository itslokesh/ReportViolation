package com.example.reportviolation.utils

import java.time.*
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    val IST: ZoneId = ZoneId.of("Asia/Kolkata")

    fun nowZonedIst(): ZonedDateTime = ZonedDateTime.now(IST)

    // ISO 8601 with offset, e.g., 2025-08-16T18:25:43+05:30
    fun nowIsoIst(): String = nowZonedIst().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    fun toIstIso(instant: Instant): String = instant.atZone(IST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    fun toIstLocalDateTime(instant: Instant): LocalDateTime = instant.atZone(IST).toLocalDateTime()

    fun formatForUi(dateTime: LocalDateTime, pattern: String = "MMM dd, yyyy HH:mm"): String {
        val zoned = dateTime.atZone(IST)
        return zoned.format(DateTimeFormatter.ofPattern(pattern))
    }
}


