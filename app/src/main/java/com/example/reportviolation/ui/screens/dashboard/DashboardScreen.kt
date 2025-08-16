package com.example.reportviolation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.reportviolation.data.remote.CitizenReportItem
import com.example.reportviolation.domain.service.LanguageManager
import com.example.reportviolation.ui.screens.reports.ReportsHistoryViewModel
import com.example.reportviolation.utils.getLocalizedViolationTypeName
import com.example.reportviolation.utils.getLocalizedStatusName
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

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
                            stringResource(R.string.home), 
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
                            stringResource(R.string.reports), 
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
                            stringResource(R.string.notifications), 
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
                            stringResource(R.string.profile), 
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
            3 -> ProfileTab(padding, navController)
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
                    text = stringResource(R.string.home),
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
                        label = stringResource(R.string.violations_reported),
                        backgroundColor = DarkBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.approvedReports.toString(),
                        label = stringResource(R.string.filter_resolved),
                        backgroundColor = MediumBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = uiState.pendingReports.toString(),
                        label = stringResource(R.string.filter_in_progress),
                        backgroundColor = LightBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            // Recent Reports Section
            item {
                Text(
                    text = stringResource(R.string.recent_reports),
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
                                text = stringResource(R.string.no_reports_yet),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.start_reporting),
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
                            text = stringResource(R.string.report_violation_button),
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
        modifier = modifier.height(130.dp),
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
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentReportItem(report: RecentReport, navController: NavController) {
    val context = LocalContext.current
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
                                            text = getLocalizedViolationTypeName(report.violationType, context),
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
    val viewModel = remember { ReportsHistoryViewModel() }
    
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
            text = stringResource(R.string.traffic_violation_reports),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        // Filter tabs only
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                FilterTab(
                    text = stringResource(R.string.filter_all),
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_submitted),
                    isSelected = selectedFilter == "Submitted",
                    onClick = { selectedFilter = "Submitted" }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_in_progress),
                    isSelected = selectedFilter == "In Progress",
                    onClick = { selectedFilter = "In Progress" }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_resolved),
                    isSelected = selectedFilter == "Resolved",
                    onClick = { selectedFilter = "Resolved" }
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
                            text = stringResource(R.string.error_loading_reports),
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
                            Text(stringResource(R.string.retry))
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
                val filteredReports: List<CitizenReportItem> = uiState.reports
                    .filter { report ->
                        val statusEnum = runCatching {
                            ReportStatus.valueOf(report.status ?: "PENDING")
                        }.getOrDefault(ReportStatus.PENDING)
                        val statusMatch = when (selectedFilter) {
                            "Submitted" -> statusEnum == ReportStatus.PENDING
                            "In Progress" -> statusEnum == ReportStatus.UNDER_REVIEW
                            "Resolved" -> statusEnum == ReportStatus.APPROVED
                            else -> true
                        }
                        val typeMatch = selectedViolationType?.let { sel ->
                            report.violationTypes?.any { it == sel.name } ?: false
                        } ?: true
                        statusMatch && typeMatch
                    }
                    .let { reports ->
                        // Sort reports
                        when (sortOrder) {
                            "Newest First" -> reports.sortedByDescending { runCatching { java.time.Instant.parse(it.timestamp) }.getOrNull() }
                            "Oldest First" -> reports.sortedBy { runCatching { java.time.Instant.parse(it.timestamp) }.getOrNull() }
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
                        text = stringResource(R.string.sort_by),
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
                        text = stringResource(R.string.filter_by),
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
                text = stringResource(R.string.notifications),
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
                        text = stringResource(R.string.no_notifications),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_reports_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(padding: PaddingValues, navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { com.example.reportviolation.ui.screens.settings.SettingsViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    // Language confirmation dialog
    if (uiState.showLanguageConfirmationDialog) {
        LanguageConfirmationDialog(
            currentLanguage = uiState.selectedLanguage,
            newLanguage = uiState.pendingLanguageChange ?: "",
            onConfirm = { viewModel.confirmLanguageChange() },
            onDismiss = { viewModel.dismissLanguageConfirmationDialog() }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(
                userName = uiState.userName,
                onProfileClick = { /* Navigate to profile edit */ }
            )
        }
        
        // Language Section
        item {
            LanguageSection(
                selectedLanguage = uiState.selectedLanguage,
                onLanguageChange = { viewModel.updateLanguage(it) },
                showLanguageDropdown = uiState.showLanguageDropdown,
                onDropdownToggle = { viewModel.toggleLanguageDropdown() }
            )
        }
        
        // Accessibility Section (Text Size and Color Contrast only)
        item {
            AccessibilitySection(
                onTextSizeClick = { /* Navigate to text size settings */ },
                onColorContrastClick = { /* Navigate to color contrast settings */ }
            )
        }
        
        // Reward Points Section
        item {
            RewardPointsSection(
                points = uiState.rewardPoints
            )
        }
        
        // Logout Button
        item {
            LogoutButton(
                onLogout = { 
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Avatar",
                modifier = Modifier.size(32.dp),
                tint = DarkBlue
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun LanguageSection(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    showLanguageDropdown: Boolean,
    onDropdownToggle: () -> Unit
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    val languages = listOf(LanguageManager.LANGUAGE_ENGLISH, LanguageManager.LANGUAGE_TAMIL)
    
    Column {
        // Section Title
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Language Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                // Selected Language Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDropdownToggle() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.getLanguageDisplayName(selectedLanguage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    
                    Icon(
                        imageVector = if (showLanguageDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown",
                        tint = Color.Gray
                    )
                }
                
                // Dropdown Options
                if (showLanguageDropdown) {
                    languages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onLanguageChange(language)
                                    onDropdownToggle()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = languageManager.getLanguageDisplayName(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            
                            if (language == selectedLanguage) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DarkBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AccessibilitySection(
    onTextSizeClick: () -> Unit,
    onColorContrastClick: () -> Unit
) {
    Column {
        // Section Title
        Text(
            text = stringResource(R.string.settings_accessibility),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                // Text Size
                AccessibilityItem(
                    title = stringResource(R.string.settings_text_size),
                    icon = Icons.Default.TextFields,
                    onClick = onTextSizeClick
                )
                
                Divider(color = Color.LightGray, thickness = 0.5.dp)
                
                // Color Contrast
                AccessibilityItem(
                    title = stringResource(R.string.settings_color_contrast),
                    icon = Icons.Default.Contrast,
                    onClick = onColorContrastClick
                )
            }
        }
    }
}

@Composable
fun AccessibilityItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.Gray
        )
    }
}

@Composable
fun RewardPointsSection(
    points: String
) {
    Column {
        // Section Title
        Text(
            text = stringResource(R.string.settings_reward_points),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = points,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            }
        }
    }
}

@Composable
fun LogoutButton(
    onLogout: () -> Unit
) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_logout),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun LanguageConfirmationDialog(
    currentLanguage: String,
    newLanguage: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    
    val currentLanguageDisplay = languageManager.getLanguageDisplayName(currentLanguage)
    val newLanguageDisplay = languageManager.getLanguageDisplayName(newLanguage)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language_change_confirmation_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // English message
                Text(
                    text = stringResource(R.string.language_change_confirmation_message_english, currentLanguageDisplay, newLanguageDisplay),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Tamil message
                Text(
                    text = stringResource(R.string.language_change_confirmation_message_tamil, currentLanguageDisplay, newLanguageDisplay),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Text(
                    text = stringResource(R.string.language_change_confirm),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
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
fun ReportCardNew(report: CitizenReportItem, navController: NavController, sourceTab: String = "home") {
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
                    text = report.violationTypes?.joinToString(", ") ?: "Report #${report.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val displayTime = runCatching {
                    val instant = java.time.Instant.parse(report.timestamp)
                    java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.of("Asia/Kolkata"))
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                }.getOrDefault(report.timestamp)
                Text(
                    text = displayTime,
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
                                getStatusColor(runCatching { ReportStatus.valueOf(report.status ?: "PENDING") }.getOrDefault(ReportStatus.PENDING)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getStatusIcon(runCatching { ReportStatus.valueOf(report.status ?: "PENDING") }.getOrDefault(ReportStatus.PENDING)),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Status text
                    Text(
                        text = getLocalizedStatusName(runCatching { ReportStatus.valueOf(report.status ?: "PENDING") }.getOrDefault(ReportStatus.PENDING), LocalContext.current),
                        style = MaterialTheme.typography.bodySmall,
                        color = getStatusColor(runCatching { ReportStatus.valueOf(report.status ?: "PENDING") }.getOrDefault(ReportStatus.PENDING)),
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
                text = stringResource(R.string.violation_types_header),
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
                            text = getLocalizedViolationTypeName(violationType, LocalContext.current),
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
                        text = stringResource(R.string.all_violation_types),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.clear_all))
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
                text = stringResource(R.string.sort_by),
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
                            text = when (option) {
                                "Newest First" -> stringResource(R.string.sort_newest_first)
                                "Oldest First" -> stringResource(R.string.sort_oldest_first)
                                else -> option
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text(stringResource(R.string.apply))
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

