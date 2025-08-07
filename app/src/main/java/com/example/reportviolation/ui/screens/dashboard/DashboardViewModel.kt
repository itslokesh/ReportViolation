package com.example.reportviolation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                
                // TODO: Load data when repositories are available
                // For now, just set loading to false
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalReports = 0,
                    approvedReports = 0,
                    pendingReports = 0,
                    totalPoints = 0
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
    val error: String? = null
) 