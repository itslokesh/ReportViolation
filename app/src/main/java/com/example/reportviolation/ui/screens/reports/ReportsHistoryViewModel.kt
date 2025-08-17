package com.example.reportviolation.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.CitizenReportsApi
import com.example.reportviolation.data.remote.CitizenReportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class ReportsHistoryUiState(
    val reports: List<CitizenReportItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

class ReportsHistoryViewModel : ViewModel() {

    private val reportsApi: CitizenReportsApi by lazy {
        val baseRetrofit = ApiClient.retrofit(OkHttpClient.Builder().build())
        val authClient = ApiClient.buildClientWithAuthenticator(baseRetrofit.create(com.example.reportviolation.data.remote.AuthApi::class.java))
        ApiClient.retrofit(authClient).create(CitizenReportsApi::class.java)
    }

    private val _uiState = MutableStateFlow(ReportsHistoryUiState())
    val uiState: StateFlow<ReportsHistoryUiState> = _uiState.asStateFlow()

    init {
        loadReports(reset = true)
    }

    private var lastLoadedAtMs: Long = 0L
    private val minReloadIntervalMs: Long = 30_000 // 30 seconds client-side throttle

    fun loadReports(reset: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (!reset && (now - lastLoadedAtMs) < minReloadIntervalMs && _uiState.value.reports.isNotEmpty()) {
                return@launch
            }
            val nextPage = if (reset) 1 else _uiState.value.page
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, page = nextPage)
            try {
                val res = reportsApi.listReports(page = nextPage, limit = 20)
                println("API_LIST_REPORTS request={page=$nextPage,limit=20}")
                println("API_LIST_REPORTS response=" + com.google.gson.Gson().toJson(res))
                if (!res.success) throw IllegalStateException(res.error ?: "Unknown error")
                val incoming = res.data?.reports ?: emptyList()
                val items = if (reset) incoming else _uiState.value.reports + incoming
                val pages = res.data?.pagination?.pages ?: nextPage
                val hasMore = nextPage < pages
                lastLoadedAtMs = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    reports = items,
                    isLoading = false,
                    hasMore = hasMore,
                    page = if (hasMore) nextPage + 1 else nextPage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reports"
                )
            }
        }
    }

    fun refreshReports() = loadReports(reset = true)
}
