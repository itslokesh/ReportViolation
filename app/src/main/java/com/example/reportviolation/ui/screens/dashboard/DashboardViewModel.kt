package com.example.reportviolation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.reportviolation.R

class DashboardViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // For now, use mock data since we don't have proper DI setup
                // In a real app, this would come from the repository
                val mockStats = mapOf(
                    "total" to 15,
                    "approved" to 8,
                    "pending" to 4,
                    "rejected" to 3
                )
                
                val totalReports = mockStats["total"] ?: 0
                val approvedReports = mockStats["approved"] ?: 0
                val pendingReports = mockStats["pending"] ?: 0
                
                // Mock recent reports
                val recentReports = listOf(
                    RecentReport(
                        violationType = "Speed Violation",
                        iconRes = R.drawable.ic_speed_violation,
                        date = "Dec 15"
                    ),
                    RecentReport(
                        violationType = "Signal Jumping",
                        iconRes = R.drawable.ic_signal_jumping,
                        date = "Dec 14"
                    ),
                    RecentReport(
                        violationType = "Wrong Side Driving",
                        iconRes = R.drawable.ic_wrong_side_driving,
                        date = "Dec 12"
                    )
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalReports = totalReports,
                    approvedReports = approvedReports,
                    pendingReports = pendingReports,
                    totalPoints = 1250,
                    recentReports = recentReports
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
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
    val violationType: String,
    val iconRes: Int,
    val date: String
) 