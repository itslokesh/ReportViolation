package com.example.reportviolation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reportviolation.R
import com.example.reportviolation.ui.navigation.Screen
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.ui.theme.LightBlue
import com.example.reportviolation.ui.theme.LightGray
import com.example.reportviolation.ui.theme.MediumBlue
import com.example.reportviolation.ui.components.ViolationIcon
import com.example.reportviolation.ui.components.ViolationIconDisplayMode
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.repository.ViolationReportRepository
import com.example.reportviolation.data.local.AppDatabase
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import com.example.reportviolation.ui.screens.reports.ReportsHistoryViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    initialTab: Int = 0
) {
    // Create ViewModel - in a real app, this would be injected via Hilt or similar DI framework
    val viewModel = remember { DashboardViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(initialTab) }
    
    // Note: Back navigation preservation will be handled by the navigation system
    // The selectedTab state will be maintained automatically
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Home, 
                            contentDescription = "Home",
                            tint = if (selectedTab == 0) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Home", 
                            color = if (selectedTab == 0) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Description, 
                            contentDescription = "Reports",
                            tint = if (selectedTab == 1) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Reports", 
                            color = if (selectedTab == 1) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            tint = if (selectedTab == 2) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Notifications", 
                            color = if (selectedTab == 2) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = "Profile",
                            tint = if (selectedTab == 3) DarkBlue else Color.Gray
                        ) 
                    },
                    label = { 
                        Text(
                            "Profile", 
                            color = if (selectedTab == 3) DarkBlue else Color.Gray
                        ) 
                    },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(padding, navController, uiState)
            1 -> ReportsTab(padding, navController)
            2 -> NotificationsTab(padding)
            3 -> ProfileTab(padding)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeTab(padding: PaddingValues, navController: NavController, uiState: DashboardUiState) {
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = {
            // Refresh dashboard data - this will trigger a reload
            // The ViewModel will handle the data loading internally
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGray)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // Summary Statistics Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = uiState.totalReports.toString(),
                        label = "Violations\nReported",
                        backgroundColor = DarkBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.approvedReports.toString(),
                        label = "Resolved",
                        backgroundColor = MediumBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.pendingReports.toString(),
                        label = "Pending",
                        backgroundColor = LightBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            // Recent Reports Section
            item {
                Text(
                    text = "Recent Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Recent Reports List
            if (uiState.recentReports.isNotEmpty()) {
                items(uiState.recentReports) { report ->
                    RecentReportItem(report = report, navController = navController)
                    if (report != uiState.recentReports.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No reports yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start reporting violations to see them here",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            // Report Violation Button
            item {
                Button(
                    onClick = { navController.navigate(Screen.ReportViolation.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Report\nViolation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Pull to refresh indicator
        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RecentReportItem(report: RecentReport, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // For demo purposes, navigate to a sample report with ID 1
                navController.navigate("${Screen.ReportDetails.route}/1")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Violation Icon
            ViolationIcon(
                violationType = report.violationType,
                displayMode = ViolationIconDisplayMode.REPORT_DETAILS
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Violation Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.violationType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            // Date
            Text(
                text = report.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReportsTab(padding: PaddingValues, navController: NavController) {
    val context = LocalContext.current
    
    // Initialize database and repository
    val database = remember { AppDatabase.getDatabase(context) }
    val duplicateDetectionService = remember { DuplicateDetectionService() }
    val jurisdictionService = remember { JurisdictionService() }
    val repository = remember { 
        ViolationReportRepository(
            database.violationReportDao(),
            duplicateDetectionService,
            jurisdictionService
        ) 
    }
    val viewModel = remember { ReportsHistoryViewModel(repository) }
    
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    
    // Filter and sort state
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedViolationType by remember { mutableStateOf<ViolationType?>(null) }
    var sortOrder by remember { mutableStateOf("Newest First") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    // Temporary state for dialogs (only applied when user clicks Apply)
    var tempSelectedViolationType by remember { mutableStateOf<ViolationType?>(null) }
    var tempSortOrder by remember { mutableStateOf("Newest First") }
    
    val filters = listOf("All", "Submitted", "In Progress", "Resolved")
    val sortOptions = listOf("Newest First", "Oldest First")
    
    // Pull to refresh state for reports
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = {
            viewModel.refreshReports()
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGray)
                .padding(padding)
        ) {
        // Header with title (matching Home tab style)
        Text(
            text = "Traffic Violation Reports",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        // Filter tabs only
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterTab(
                    text = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }
        
        // Reports list
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading reports",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshReports() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.reports.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No reports yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Submit your first violation report to see it here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                val filteredReports = uiState.reports
                    .filter { report ->
                        // Status filter
                        val statusMatch = when (selectedFilter) {
                            "Submitted" -> report.status == ReportStatus.PENDING
                            "In Progress" -> report.status == ReportStatus.UNDER_REVIEW
                            "Resolved" -> report.status == ReportStatus.APPROVED
                            else -> true
                        }
                        
                        // Violation type filter
                        val typeMatch = selectedViolationType?.let { report.violationType == it } ?: true
                        
                        statusMatch && typeMatch
                    }
                    .let { reports ->
                        // Sort reports
                        when (sortOrder) {
                            "Newest First" -> reports.sortedByDescending { it.createdAt }
                            "Oldest First" -> reports.sortedBy { it.createdAt }
                            else -> reports
                        }
                    }
                
                if (filteredReports.isEmpty()) {
                    // Show "No reports found" when filters return no results
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reports found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try adjusting your filters or submit a new report",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(filteredReports) { report ->
                            ReportCardNew(
                                report = report, 
                                navController = navController,
                                sourceTab = "reports"
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom controls - Sort and Filter buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sort button
            Button(
                onClick = { 
                    tempSortOrder = sortOrder // Initialize temp with current value
                    showSortDialog = true 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sortOrder != "Newest First") DarkBlue else Color.White,
                    contentColor = if (sortOrder != "Newest First") Color.White else DarkBlue
                ),
                border = if (sortOrder != "Newest First") null else BorderStroke(1.dp, DarkBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Filter button
            Button(
                onClick = { 
                    tempSelectedViolationType = selectedViolationType // Initialize temp with current value
                    showFilterDialog = true 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedViolationType != null) DarkBlue else Color.White,
                    contentColor = if (selectedViolationType != null) Color.White else DarkBlue
                ),
                border = if (selectedViolationType != null) null else BorderStroke(1.dp, DarkBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Pull to refresh indicator
    PullRefreshIndicator(
        refreshing = uiState.isLoading,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter)
    )
}
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            selectedViolationType = tempSelectedViolationType,
            onViolationTypeSelected = { tempSelectedViolationType = it },
            onDismiss = { 
                showFilterDialog = false
                tempSelectedViolationType = selectedViolationType // Reset to current value
            },
            onClearFilters = { 
                tempSelectedViolationType = null
            },
            onApply = {
                selectedViolationType = tempSelectedViolationType
                showFilterDialog = false
            }
        )
    }
    
    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSortOrder = tempSortOrder,
            sortOptions = sortOptions,
            onSortOrderSelected = { tempSortOrder = it },
            onDismiss = { 
                showSortDialog = false
                tempSortOrder = sortOrder // Reset to current value
            },
            onApply = {
                sortOrder = tempSortOrder
                showSortDialog = false
            }
        )
    }
}

@Composable
fun NotificationsTab(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(padding)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your account settings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// New Reports Tab Components
@Composable
fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFFE3F2FD) else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) DarkBlue else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ReportCardNew(report: ViolationReport, navController: NavController, sourceTab: String = "home") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("${Screen.ReportDetails.route}/${report.id}?sourceTab=$sourceTab")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Report info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Case #${report.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = report.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            // Right side - Status timeline
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeline line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color(0xFFE0E0E0))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Status indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                getStatusColor(report.status),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getStatusIcon(report.status),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Status text
                    Text(
                        text = getStatusText(report.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = getStatusColor(report.status),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    selectedViolationType: ViolationType?,
    onViolationTypeSelected: (ViolationType?) -> Unit,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Filter by Violation Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                ViolationType.values().forEach { violationType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViolationTypeSelected(violationType) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedViolationType == violationType,
                            onClick = { onViolationTypeSelected(violationType) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        ViolationIcon(
                            violationType = violationType,
                            displayMode = ViolationIconDisplayMode.QUICK_SELECTION,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = violationType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViolationTypeSelected(null) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedViolationType == null,
                        onClick = { onViolationTypeSelected(null) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "All Violation Types",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) {
                Text("Clear All")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSortOrder: String,
    sortOptions: List<String>,
    onSortOrderSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Sort by Date",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                sortOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortOrderSelected(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOrder == option,
                            onClick = { onSortOrderSelected(option) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Apply")
            }
        }
    )
}

private fun getStatusColor(status: ReportStatus): Color {
    return when (status) {
        ReportStatus.PENDING -> Color(0xFF1976D2)      // Blue
        ReportStatus.UNDER_REVIEW -> Color(0xFFFF9800) // Orange
        ReportStatus.APPROVED -> Color(0xFF4CAF50)     // Green
        ReportStatus.REJECTED -> Color(0xFFF44336)     // Red
        ReportStatus.DUPLICATE -> Color(0xFF9E9E9E)    // Gray
    }
}

private fun getStatusIcon(status: ReportStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        ReportStatus.PENDING -> Icons.Default.KeyboardArrowDown
        ReportStatus.UNDER_REVIEW -> Icons.Default.Schedule
        ReportStatus.APPROVED -> Icons.Default.Check
        ReportStatus.REJECTED -> Icons.Default.Close
        ReportStatus.DUPLICATE -> Icons.Default.Warning
    }
}

private fun getStatusText(status: ReportStatus): String {
    return when (status) {
        ReportStatus.PENDING -> "Submitted"
        ReportStatus.UNDER_REVIEW -> "In Progress"
        ReportStatus.APPROVED -> "Resolved"
        ReportStatus.REJECTED -> "Rejected"
        ReportStatus.DUPLICATE -> "Duplicate"
    }
}