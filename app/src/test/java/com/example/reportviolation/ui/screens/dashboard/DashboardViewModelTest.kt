package com.example.reportviolation.ui.screens.dashboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DashboardViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have default values`() = runTest {
        // When
        val initialState = viewModel.uiState.first()
        
        // Then
        assertFalse("Initial loading should be false", initialState.isLoading)
        assertEquals("Initial total reports should be 0", 0, initialState.totalReports)
        assertEquals("Initial approved reports should be 0", 0, initialState.approvedReports)
        assertEquals("Initial pending reports should be 0", 0, initialState.pendingReports)
        assertEquals("Initial total points should be 0", 0, initialState.totalPoints)
        assertNull("Initial error should be null", initialState.error)
    }
    
    @Test
    fun `loadDashboardData should update state with correct values`() = runTest {
        // Given
        val expectedState = DashboardUiState(
            isLoading = false,
            totalReports = 0,
            approvedReports = 0,
            pendingReports = 0,
            totalPoints = 0,
            error = null
        )
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        val actualState = viewModel.uiState.first()
        
        // Then
        assertEquals("State should match expected values", expectedState, actualState)
    }
    
    @Test
    fun `refreshData should reload dashboard data`() = runTest {
        // Given
        val initialState = viewModel.uiState.first()
        
        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()
        val refreshedState = viewModel.uiState.first()
        
        // Then
        assertEquals("Refreshed state should match initial state", initialState, refreshedState)
    }
    
    @Test
    fun `uiState should emit values correctly`() = runTest {
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        
        // Then
        assertNotNull("State should not be null", state)
        assertFalse("Loading should be false after initialization", state.isLoading)
        assertEquals("Total reports should be 0", 0, state.totalReports)
        assertEquals("Approved reports should be 0", 0, state.approvedReports)
        assertEquals("Pending reports should be 0", 0, state.pendingReports)
        assertEquals("Total points should be 0", 0, state.totalPoints)
    }
    
    @Test
    fun `DashboardUiState copy should work correctly`() {
        // Given
        val originalState = DashboardUiState()
        
        // When
        val copiedState = originalState.copy(
            totalReports = 5,
            approvedReports = 3,
            pendingReports = 2,
            totalPoints = 100
        )
        
        // Then
        assertEquals("Total reports should be updated", 5, copiedState.totalReports)
        assertEquals("Approved reports should be updated", 3, copiedState.approvedReports)
        assertEquals("Pending reports should be updated", 2, copiedState.pendingReports)
        assertEquals("Total points should be updated", 100, copiedState.totalPoints)
        assertEquals("Loading should remain unchanged", originalState.isLoading, copiedState.isLoading)
        assertEquals("Error should remain unchanged", originalState.error, copiedState.error)
    }
    
    @Test
    fun `DashboardUiState should have correct default values`() {
        // When
        val state = DashboardUiState()
        
        // Then
        assertFalse("Default loading should be false", state.isLoading)
        assertEquals("Default total reports should be 0", 0, state.totalReports)
        assertEquals("Default approved reports should be 0", 0, state.approvedReports)
        assertEquals("Default pending reports should be 0", 0, state.pendingReports)
        assertEquals("Default total points should be 0", 0, state.totalPoints)
        assertNull("Default error should be null", state.error)
    }
    
    @Test
    fun `DashboardUiState should handle error state`() {
        // Given
        val errorMessage = "Test error message"
        
        // When
        val state = DashboardUiState(error = errorMessage)
        
        // Then
        assertEquals("Error should be set correctly", errorMessage, state.error)
        assertFalse("Loading should be false in error state", state.isLoading)
    }
    
    @Test
    fun `DashboardUiState should handle loading state`() {
        // When
        val state = DashboardUiState(isLoading = true)
        
        // Then
        assertTrue("Loading should be true", state.isLoading)
        assertEquals("Total reports should be 0 in loading state", 0, state.totalReports)
    }
}
