package com.example.reportviolation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.reportviolation.utils.DateTimeUtils
import androidx.compose.ui.res.stringResource
import com.example.reportviolation.data.remote.CitizenReportItem
import com.example.reportviolation.domain.service.LanguageManager
import com.example.reportviolation.ui.screens.reports.ReportsHistoryViewModel
import com.example.reportviolation.utils.getLocalizedViolationTypeName
import com.example.reportviolation.utils.getLocalizedStatusName
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.example.reportviolation.data.remote.NotificationItem
import com.example.reportviolation.ui.screens.auth.OtpNetworkBridge
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.platform.LocalContext
import com.example.reportviolation.utils.push.AppFirebaseMessagingService

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
    var reportsInitialFilter by remember { mutableStateOf<String?>(null) }
    
    // Note: Back navigation preservation will be handled by the navigation system
    // The selectedTab state will be maintained automatically
    
    // Tabs are now owned by root Scaffold in AppNavigation
    Box(modifier = Modifier.fillMaxSize()) {
        // Local padding placeholder to satisfy child composables expecting a PaddingValues parameter
        val padding = PaddingValues(0.dp)
        when (selectedTab) {
            0 -> HomeTab(
                padding = padding,
                navController = navController,
                uiState = uiState,
                onNavigateToReports = { filter ->
                    reportsInitialFilter = filter
                    selectedTab = 1
                },
                onRefresh = { viewModel.refreshData() }
            )
            1 -> ReportsTab(padding, navController, initialFilter = reportsInitialFilter)
            2 -> NotificationsTab(padding)
            3 -> ProfileTab(padding, navController)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeTab(
    padding: PaddingValues,
    navController: NavController,
    uiState: DashboardUiState,
    onNavigateToReports: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = onRefresh
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header
            item {
                val settingsVm = remember { com.example.reportviolation.ui.screens.settings.SettingsViewModel() }
                val settings by settingsVm.uiState.collectAsState()
                LaunchedEffect(Unit) { settingsVm.loadUserSettings() }
                Text(
                    text = stringResource(R.string.hello_name, settings.userName),
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToReports("Submitted") }
                    )
                    StatCard(
                        value = uiState.submittedReports.toString(),
                        label = stringResource(R.string.to_be_reviewed),
                        backgroundColor = MediumBlue,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToReports("Submitted") }
                    )
                    StatCard(
                        value = uiState.pendingReports.toString(),
                        label = stringResource(R.string.filter_under_review),
                        backgroundColor = LightBlue,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToReports("Under Review") }
                    )
                }
            }
            
            // Second row for Approved and Rejected (same width as first-row cards)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val spacing = 12.dp
                    val cardWidth = (maxWidth - spacing * 2) / 3f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StatCard(
                            value = uiState.approvedReports.toString(),
                            label = stringResource(R.string.filter_approved),
                            backgroundColor = Color(0xFF4CAF50),
                            modifier = Modifier
                                .width(cardWidth)
                                .clickable { onNavigateToReports("Approved") }
                        )
                        Spacer(modifier = Modifier.width(spacing))
                        StatCard(
                            value = uiState.rejectedReports.toString(),
                            label = stringResource(R.string.filter_rejected),
                            backgroundColor = Color(0xFFE53935),
                            modifier = Modifier
                                .width(cardWidth)
                                .clickable { onNavigateToReports("Rejected") }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
            
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
                            containerColor = MaterialTheme.colorScheme.surface
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.start_reporting),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
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
                navController.navigate("${Screen.ReportDetails.route}/${report.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Date
            Text(
                text = formatReportRelativeIst(report.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReportsTab(padding: PaddingValues, navController: NavController, initialFilter: String? = null) {
    val viewModel = remember { ReportsHistoryViewModel() }
    
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    
    // Filter and sort state
    var selectedFilter by remember { mutableStateOf(initialFilter ?: "All") }
    var selectedViolationType by remember { mutableStateOf<ViolationType?>(null) }
    var selectedViolationTypes by remember { mutableStateOf<Set<ViolationType>>(emptySet()) }
    var sortOrder by remember { mutableStateOf("Newest First") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    // Temporary state for dialogs (only applied when user clicks Apply)
    var tempSelectedViolationType by remember { mutableStateOf<ViolationType?>(null) }
    var tempSortOrder by remember { mutableStateOf("Newest First") }
    
    val filters = listOf("All", "Submitted", "Under Review", "Approved", "Rejected")
    var accumDragX by remember { mutableStateOf(0f) }
    var lastFilterIndex by remember { mutableStateOf(0) }
    var animDirection by remember { mutableStateOf(0) } // -1 left, 1 right
    val density = LocalDensity.current
    val sortOptions = listOf("Newest First", "Oldest First")
    val controlsBarHeight = 44.dp
    
    // Controls row will be placed at the bottom of this tab
    
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
            .pointerInput(selectedFilter) {
                // Swipe left/right to change quick filter
                val thresholdPx = with(density) { 64.dp.toPx() }
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        accumDragX += dragAmount
                    },
                    onDragEnd = {
                        if (accumDragX > thresholdPx) {
                            // Swiped right → previous filter
                            val idx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                            val prev = if (idx <= 0) filters.last() else filters[idx - 1]
                            animDirection = -1
                            selectedFilter = prev
                            lastFilterIndex = filters.indexOf(prev)
                        } else if (accumDragX < -thresholdPx) {
                            // Swiped left → next filter
                            val idx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                            val next = if (idx < 0) filters.first() else filters[(idx + 1) % filters.size]
                            animDirection = 1
                            selectedFilter = next
                            lastFilterIndex = filters.indexOf(next)
                        }
                        accumDragX = 0f
                    },
                    onDragCancel = { accumDragX = 0f },
                    onDragStart = { accumDragX = 0f }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 0.dp)
        ) {
        // Header with title (matching Home tab style)
        Text(
            text = stringResource(R.string.traffic_violation_reports),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        // Filter tabs only with slide animation on change
        androidx.compose.animation.AnimatedContent(
            targetState = selectedFilter,
            transitionSpec = {
                androidx.compose.animation.slideInHorizontally { fullWidth -> fullWidth } +
                    androidx.compose.animation.fadeIn() togetherWith
                    (androidx.compose.animation.slideOutHorizontally { fullWidth -> -fullWidth / 2 } +
                        androidx.compose.animation.fadeOut())
            },
            label = "filtersAnimated"
        ) { _ ->
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
                    onClick = {
                        val oldIdx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                        val newIdx = 0
                        animDirection = if (newIdx - oldIdx >= 0) 1 else -1
                        selectedFilter = "All"
                    }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_submitted),
                    isSelected = selectedFilter == "Submitted",
                    onClick = {
                        val oldIdx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                        val newIdx = 1
                        animDirection = if (newIdx - oldIdx >= 0) 1 else -1
                        selectedFilter = "Submitted"
                    }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_under_review),
                    isSelected = selectedFilter == "Under Review",
                    onClick = {
                        val oldIdx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                        val newIdx = 2
                        animDirection = if (newIdx - oldIdx >= 0) 1 else -1
                        selectedFilter = "Under Review"
                    }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_approved),
                    isSelected = selectedFilter == "Approved",
                    onClick = {
                        val oldIdx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                        val newIdx = 3
                        animDirection = if (newIdx - oldIdx >= 0) 1 else -1
                        selectedFilter = "Approved"
                    }
                )
            }
            item {
                FilterTab(
                    text = stringResource(R.string.filter_rejected),
                    isSelected = selectedFilter == "Rejected",
                    onClick = {
                        val oldIdx = filters.indexOf(selectedFilter).coerceAtLeast(0)
                        val newIdx = 4
                        animDirection = if (newIdx - oldIdx >= 0) 1 else -1
                        selectedFilter = "Rejected"
                    }
                )
            }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Reports list with sliding animation based on selectedFilter changes
        AnimatedContent(
            targetState = selectedFilter,
            transitionSpec = {
                val dir = animDirection
                if (dir >= 0) {
                    slideInHorizontally { fullWidth -> fullWidth } + fadeIn() togetherWith
                        (slideOutHorizontally { fullWidth -> -fullWidth / 2 } + fadeOut())
                } else {
                    slideInHorizontally { fullWidth -> -fullWidth } + fadeIn() togetherWith
                        (slideOutHorizontally { fullWidth -> fullWidth / 2 } + fadeOut())
                }
            },
            label = "reportsListAnimated"
        ) { _ ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
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
                            "Under Review" -> statusEnum == ReportStatus.UNDER_REVIEW
                            "Approved" -> statusEnum == ReportStatus.APPROVED
                            "Rejected" -> statusEnum == ReportStatus.REJECTED
                            else -> true
                        }
                        val typeMatch = if (selectedViolationTypes.isEmpty()) {
                            true
                        } else {
                            // Prefer mediaMetadata.violationTypes if present
                            val namesFromMedia = runCatching {
                                val json = report.mediaMetadata
                                if (!json.isNullOrBlank()) {
                                    val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
                                    obj.getAsJsonArray("violationTypes")?.mapNotNull { it.asString }
                                } else null
                            }.getOrNull()
                            val names = (namesFromMedia ?: report.violationTypes ?: emptyList())
                            // UNION semantics: include if any selected type matches
                            selectedViolationTypes.any { sel -> names.any { it.equals(sel.name, ignoreCase = true) } }
                        }
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
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
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
                        contentPadding = PaddingValues(top = 16.dp, bottom = controlsBarHeight + 8.dp)
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
        }

        // Bottom controls overlay as Popup so it draws above all content
        val bottomInsetPx = with(LocalDensity.current) { padding.calculateBottomPadding().roundToPx() }
        // Enclosing light rectangle container; not a dark/black box
        Popup(
            alignment = Alignment.BottomCenter,
            offset = IntOffset(0, -bottomInsetPx),
            properties = PopupProperties(focusable = false, clippingEnabled = false)
        ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .height(controlsBarHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isSortActive = sortOrder != "Newest First"
            OutlinedButton(
                onClick = {
                    tempSortOrder = sortOrder
                    showSortDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSortActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                    contentColor = if (isSortActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, if (isSortActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSortActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            val isFilterActive = (selectedViolationType != null) || selectedViolationTypes.isNotEmpty()
            OutlinedButton(
                onClick = {
                    tempSelectedViolationType = selectedViolationType
                    showFilterDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isFilterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                    contentColor = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.filter_by),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        }
        }

        // Pull to refresh indicator at top center
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState
            )
        }
    }
}
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            selectedViolationType = tempSelectedViolationType,
            onViolationTypeSelected = { tempSelectedViolationType = it },
            selectedTypes = selectedViolationTypes,
            onSelectedTypesChange = { selectedViolationTypes = it },
            onDismiss = {
                showFilterDialog = false
                tempSelectedViolationType = selectedViolationType // Reset to current value
            },
            onClearFilters = {
                tempSelectedViolationType = null
                selectedViolationTypes = emptySet()
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
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        runCatching {
            val res = OtpNetworkBridge.listNotifications()
            if (res.success) {
                notifications = res.data?.notifications ?: emptyList()
            } else {
                error = res.message ?: res.error ?: "Failed to load"
            }
        }.onFailure { throwable ->
            error = throwable.message
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.notifications),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (notifications.isNotEmpty()) {
                TextButton(onClick = {
                    isLoading = true
                    error = null
                    scope.launch {
                        runCatching { OtpNetworkBridge.markAllNotificationsRead() }
                            .onSuccess { res ->
                                if (res.success) notifications = emptyList() else error = res.message ?: res.error
                            }
                            .onFailure { error = it.message }
                        isLoading = false
                    }
                }) {
                    Text(text = stringResource(R.string.clear_all))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            !error.isNullOrBlank() -> {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
            notifications.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationListItem(
                            item = item,
                            onDelete = {
                                scope.launch {
                                    runCatching { OtpNetworkBridge.markNotificationRead(item.id) }
                                        .onSuccess { res ->
                                            if (res.success) notifications = notifications.filterNot { it.id == item.id } else error = res.message ?: res.error
                                        }
                                        .onFailure { error = it.message }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationListItem(
    item: NotificationItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.message ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = formatReportRelativeIst(item.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.delete))
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
        viewModel.loadUserSettings()
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

    // Trigger activity recreation when language changes to refresh all Composables with new resources
    if (uiState.shouldRecreate) {
        val activity = (context as? android.app.Activity)
        LaunchedEffect(uiState.shouldRecreate) {
            viewModel.markRecreateHandled()
            activity?.recreate()
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
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
                onColorContrastClick = { com.example.reportviolation.ui.theme.ThemeController.toggle() }
            )
        }

        // Feedbacks Section (Submit Feedback, Show Feedbacks)
        item {
            FeedbacksSection(
                onSubmitFeedback = { navController.navigate(com.example.reportviolation.ui.navigation.Screen.Feedback.route) },
                onShowFeedbacks = { navController.navigate(com.example.reportviolation.ui.navigation.Screen.FeedbackList.route) }
            )
        }
        
        // Reward Points Section
        item {
            RewardPointsSection(
                points = uiState.rewardPoints,
                onTransactionsClick = { navController.navigate(com.example.reportviolation.ui.navigation.Screen.RewardTransactions.route) }
            )
        }

        // Old feedback button removed; now part of FeedbacksSection
        
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
            color = MaterialTheme.colorScheme.onSurface
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Language Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = if (showLanguageDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.onSurface
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
    onColorContrastClick: () -> Unit
) {
    Column {
        // Section Title
        Text(
            text = stringResource(R.string.settings_accessibility),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
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
fun FeedbacksSection(
    onSubmitFeedback: () -> Unit,
    onShowFeedbacks: () -> Unit
) {
    Column {
        // Section Title
        Text(
            text = stringResource(com.example.reportviolation.R.string.feedbacks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubmitFeedback() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.example.reportviolation.R.string.submit_feedback),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DarkBlue
                    )
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowFeedbacks() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.example.reportviolation.R.string.show_feedbacks),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DarkBlue
                    )
                }
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
            color = MaterialTheme.colorScheme.onSurface
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
    points: String,
    onTransactionsClick: () -> Unit
) {
    Column {
        // Section Title
        Text(
            text = "Reward",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reward Balance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = points,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransactionsClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reward Transactions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DarkBlue
                    )
                }
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                // Capture current Reports state if available
                val backEntry = navController.currentBackStackEntry
                // Navigate preserving context; adding query keeps route simple while state lives in back stack
                navController.navigate("${Screen.ReportDetails.route}/${report.id}?sourceTab=${sourceTab}")
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val displayTime = formatReportRelativeIst(report.timestamp)
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    selectedTypes: Set<ViolationType> = emptySet(),
    onSelectedTypesChange: (Set<ViolationType>) -> Unit = {},
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
                    val isSelected = selectedTypes.contains(violationType)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = if (isSelected) selectedTypes - violationType else selectedTypes + violationType
                                onSelectedTypesChange(next)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val next = if (checked) selectedTypes + violationType else selectedTypes - violationType
                                onSelectedTypesChange(next)
                            }
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
                        .clickable { onSelectedTypesChange(emptySet()) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedTypes.isEmpty(),
                        onCheckedChange = { onSelectedTypesChange(emptySet()) }
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

@Composable
private fun formatReportRelativeIst(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    val instant = runCatching { java.time.Instant.parse(timestamp) }
        .getOrElse {
            runCatching { java.time.OffsetDateTime.parse(timestamp).toInstant() }
                .getOrElse { runCatching { java.time.ZonedDateTime.parse(timestamp).toInstant() }.getOrNull() }
        } ?: return timestamp

    val nowIst = DateTimeUtils.nowZonedIst()
    val timeIst = instant.atZone(DateTimeUtils.IST)
    val duration = java.time.Duration.between(timeIst, nowIst)
    val seconds = duration.seconds
    val minutes = duration.toMinutes()

    return when {
        seconds < 60 && seconds >= 0 -> stringResource(R.string.time_just_now)
        minutes in 1..59 -> stringResource(R.string.time_minutes_ago, minutes.toInt())
        timeIst.toLocalDate().isEqual(nowIst.toLocalDate()) ->
            stringResource(R.string.time_today_at, DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a"))
        timeIst.toLocalDate().plusDays(1).isEqual(nowIst.toLocalDate()) ->
            stringResource(R.string.time_yesterday_at, DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a"))
        timeIst.year == nowIst.year ->
            DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM, hh:mm a")
        else ->
            DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM yyyy, hh:mm a")
    }
}

