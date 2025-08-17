package com.example.reportviolation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.reportviolation.R
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.CitizenReportItem
import com.example.reportviolation.data.remote.CitizenReportsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class DashboardViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private var lastLoadedAtMs: Long = 0L
    private val minReloadIntervalMs: Long = 30_000 // 30 seconds client-side throttle

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                if ((now - lastLoadedAtMs) < minReloadIntervalMs && _uiState.value.recentReports.isNotEmpty()) {
                    return@launch
                }
                _uiState.value = _uiState.value.copy(isLoading = true)

                val base = ApiClient.retrofit(OkHttpClient.Builder().build())
                val authClient = ApiClient.buildClientWithAuthenticator(base.create(com.example.reportviolation.data.remote.AuthApi::class.java))
                val reportsApi = ApiClient.retrofit(authClient).create(CitizenReportsApi::class.java)

                // Fetch first page to get pagination info
                val first = reportsApi.listReports(page = 1, limit = 50)
                if (!(first.success)) throw IllegalStateException(first.error ?: first.message ?: "Failed to load reports")
                val firstPage = first.data ?: throw IllegalStateException("Empty response")

                // Accumulate all pages if needed for accurate counts
                val allItems = mutableListOf<CitizenReportItem>()
                allItems.addAll(firstPage.reports)
                val totalPages = firstPage.pagination.pages
                if (totalPages > 1) {
                    for (p in 2..totalPages) {
                        val pageRes = reportsApi.listReports(page = p, limit = 50)
                        if (pageRes.success) {
                            pageRes.data?.reports?.let { allItems.addAll(it) }
                        }
                    }
                }

                val totalReports = firstPage.pagination.total
                val approvedReports = allItems.count { (it.status ?: "").equals("APPROVED", ignoreCase = true) }
                val inProgressReports = allItems.count { (it.status ?: "").equals("UNDER_REVIEW", ignoreCase = true) }

                // Build latest 3 recent reports
                val latestItems = allItems
                    .sortedByDescending { runCatching { java.time.Instant.parse(it.timestamp) }.getOrNull() }
                    .take(3)

                // Fetch details for the latest 3 if primary type is missing
                val latest3 = latestItems.map { item ->
                    val primaryTypeStr = item.violationTypes?.firstOrNull() ?: run {
                        val detail = runCatching { reportsApi.getReport(item.id).data }.getOrNull()
                        val fromMeta = runCatching {
                            val json = detail?.mediaMetadata
                            if (!json.isNullOrBlank()) {
                                val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
                                obj.getAsJsonArray("violationTypes")?.firstOrNull()?.asString
                            } else null
                        }.getOrNull()
                        fromMeta ?: detail?.violationType
                    }

                    RecentReport(
                        id = item.id,
                        violationType = mapRemoteTypeToEnum(primaryTypeStr),
                        date = item.timestamp
                    )
                }

                lastLoadedAtMs = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalReports = totalReports,
                    approvedReports = approvedReports,
                    pendingReports = inProgressReports,
                    totalPoints = 1250,
                    recentReports = latest3
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun mapRemoteTypeToEnum(remote: String?): ViolationType {
        if (remote.isNullOrBlank()) return ViolationType.OTHERS
        // Direct enum match
        runCatching { return ViolationType.valueOf(remote.trim().uppercase()) }.getOrNull()
        // Display name match
        ViolationType.values().firstOrNull { it.displayName.equals(remote, ignoreCase = true) }?.let { return it }
        // Normalize common aliases
        val norm = remote.trim().lowercase()
            .replace('-', ' ')
            .replace('_', ' ')
            .replace("  ", " ")
        return when {
            "wrong" in norm && "side" in norm -> ViolationType.WRONG_SIDE_DRIVING
            ("no" in norm && "parking" in norm) || ("parking" in norm) -> ViolationType.NO_PARKING_ZONE
            ("signal" in norm && ("jump" in norm || "violation" in norm)) -> ViolationType.SIGNAL_JUMPING
            ("speed" in norm) || ("speeding" in norm) -> ViolationType.SPEED_VIOLATION
            ("helmet" in norm) || ("seatbelt" in norm) -> ViolationType.HELMET_SEATBELT_VIOLATION
            ("mobile" in norm) || ("phone" in norm) -> ViolationType.MOBILE_PHONE_USAGE
            ("lane" in norm && ("cut" in norm || "change" in norm)) -> ViolationType.LANE_CUTTING
            ("drunk" in norm) || ("intox" in norm) -> ViolationType.DRUNK_DRIVING_SUSPECTED
            else -> ViolationType.OTHERS
        }
    }
    

    
    fun refreshData() {
        loadDashboardData()
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalReports: Int = 0,
    val approvedReports: Int = 0,
    val pendingReports: Int = 0,
    val totalPoints: Int = 0,
    val recentReports: List<RecentReport> = emptyList(),
    val error: String? = null
)

data class RecentReport(
    val id: String,
    val violationType: ViolationType,
    val date: String
)