package com.example.reportviolation.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.repository.ViolationReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsHistoryUiState(
    val reports: List<ViolationReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportsHistoryViewModel(
    private val repository: ViolationReportRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportsHistoryUiState())
    val uiState: StateFlow<ReportsHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadReports()
    }
    
    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val reports = repository.getAllReports()
                _uiState.value = _uiState.value.copy(
                    reports = reports,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reports"
                )
            }
        }
    }
    
    fun refreshReports() {
        loadReports()
    }
}
