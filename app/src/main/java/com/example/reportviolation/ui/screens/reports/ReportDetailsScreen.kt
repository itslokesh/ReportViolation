package com.example.reportviolation.ui.screens.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.reportviolation.R
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.repository.ViolationReportRepository
import com.example.reportviolation.data.local.AppDatabase
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ReportDetailsScreen(
    navController: NavController,
    reportId: Long
) {
    val context = LocalContext.current
    val repository = remember {
        ViolationReportRepository(
            AppDatabase.getDatabase(context).violationReportDao(),
            DuplicateDetectionService(),
            JurisdictionService()
        )
    }
    
    var report by remember { mutableStateOf<ViolationReport?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Function to load report data
    suspend fun loadReport() {
        try {
            report = repository.getViolationReportById(reportId)
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }
    
    LaunchedEffect(reportId) {
        loadReport()
    }
    
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Launch a coroutine to load the report
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                loadReport()
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading && !isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                report?.let { violationReport ->
                    ReportDetailsContent(
                        report = violationReport,
                        modifier = Modifier.padding(padding)
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Report not found")
                    }
                }
            }
            
            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ReportDetailsContent(
    report: ViolationReport,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Incident Visual Section
        IncidentVisualSection(report = report)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Incident Details Section
        IncidentDetailsSection(report = report)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Timeline Section
        StatusTimelineSection(report = report)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun IncidentVisualSection(report: ViolationReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF1976D2))
        ) {
            if (report.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(report.photoUri),
                    contentDescription = "Incident Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder image
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Placeholder",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun IncidentDetailsSection(report: ViolationReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Incident Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Time
            DetailRow(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = formatDateTime(report.timestamp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location
            LocationDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Location",
                address = report.address,
                latitude = report.latitude,
                longitude = report.longitude
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Violation Type
            DetailRow(
                icon = Icons.Default.Warning,
                label = "Violation Type",
                value = report.violationType.displayName
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF1976D2)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LocationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    address: String,
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF1976D2)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            // Clickable location with Google Maps link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    
                    // Check if Google Maps is available
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Fallback to browser with Google Maps web URL
                        val webUrl = "https://www.google.com/maps?q=$latitude,$longitude"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                        context.startActivity(browserIntent)
                    }
                }
            ) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open in Google Maps",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun StatusTimelineSection(report: ViolationReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Timeline items
            TimelineItem(
                status = "Received",
                date = formatDateTime(report.createdAt),
                isCompleted = true,
                isFirst = true
            )
            
            TimelineItem(
                status = "Submitted",
                date = formatDateTime(report.createdAt.plusMinutes(15)),
                isCompleted = true
            )
            
            TimelineItem(
                status = "In Review",
                date = formatDateTime(report.createdAt.plusDays(1)),
                isCompleted = report.status != ReportStatus.PENDING,
                isCurrent = report.status == ReportStatus.UNDER_REVIEW
            )
            
            TimelineItem(
                status = "Resolved",
                date = if (report.status == ReportStatus.APPROVED) {
                    formatDateTime(report.reviewTimestamp ?: report.updatedAt)
                } else {
                    "Pending"
                },
                isCompleted = report.status == ReportStatus.APPROVED,
                isLast = true,
                isResolved = report.status == ReportStatus.APPROVED
            )
        }
    }
}

@Composable
fun TimelineItem(
    status: String,
    date: String,
    isCompleted: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isCurrent: Boolean = false,
    isResolved: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline line and icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                        )
                )
            }
            
            // Status icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        when {
                            isResolved -> Color(0xFF4CAF50)
                            isCurrent -> Color(0xFF1976D2)
                            isCompleted -> Color(0xFF1976D2)
                            else -> Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isResolved -> Icons.Default.Check
                        isCurrent -> Icons.Default.Refresh
                        isCompleted -> Icons.Default.Check
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Status details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    isResolved -> Color(0xFF4CAF50)
                    isCurrent -> Color(0xFF1976D2)
                    isCompleted -> Color.Black
                    else -> Color.Gray
                }
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, h:mm a", Locale.ENGLISH)
    return dateTime.format(formatter)
}

