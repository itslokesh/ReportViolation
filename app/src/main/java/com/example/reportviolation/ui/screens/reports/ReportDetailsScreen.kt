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
import androidx.compose.ui.res.stringResource
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
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.CitizenReportsApi
import com.example.reportviolation.data.remote.CitizenReportDetail
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.repository.ViolationReportRepository
import com.example.reportviolation.data.local.AppDatabase
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import com.example.reportviolation.ui.navigation.Screen
import com.example.reportviolation.utils.getLocalizedViolationTypeName
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ReportDetailsScreen(
    navController: NavController,
    reportId: Long,
    sourceTab: String = "home"
) {
    val context = LocalContext.current
    val reportsApi: CitizenReportsApi = remember {
        val baseRetrofit = ApiClient.retrofit(okhttp3.OkHttpClient.Builder().build())
        val authClient = ApiClient.buildClientWithAuthenticator(baseRetrofit.create(com.example.reportviolation.data.remote.AuthApi::class.java))
        ApiClient.retrofit(authClient).create(CitizenReportsApi::class.java)
    }

    var report by remember { mutableStateOf<CitizenReportDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Function to load report data
    suspend fun loadReport() {
        try {
            val res = reportsApi.getReport(reportId.toString())
            report = res.data
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
    
    // Handle system back gesture and back button
    BackHandler {
        // Debug: Print the sourceTab value
        println("BackHandler triggered. sourceTab: $sourceTab")
        
        // Navigate back to the correct tab
        if (sourceTab == "reports") {
            println("BackHandler: Navigating to dashboard with reports tab")
            // Navigate back to dashboard with reports tab selected
            navController.navigate("${Screen.Dashboard.route}?initialTab=1") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            println("BackHandler: Using default navigateUp")
            navController.navigateUp()
        }
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
                title = { Text(stringResource(R.string.report_details)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Trigger the same logic as BackHandler
                        if (sourceTab == "reports") {
                            navController.navigate("${Screen.Dashboard.route}?initialTab=1") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }) {
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
                report?.let { detail ->
                    ReportDetailsContent(
                        report = detail,
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
    report: CitizenReportDetail,
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
fun IncidentVisualSection(report: CitizenReportDetail) {
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
            if (report.photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(report.photoUrl),
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
fun IncidentDetailsSection(report: CitizenReportDetail) {
    val context = LocalContext.current
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
                text = stringResource(R.string.report_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Time
            DetailRow(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.date_time),
                value = report.timestamp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location
            LocationDetailRow(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.location),
                address = report.addressEncrypted,
                latitude = report.latitude,
                longitude = report.longitude
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Violation Type
            DetailRow(
                icon = Icons.Default.Warning,
                label = stringResource(R.string.violation_types),
                value = buildString {
                    val typesFromMedia = runCatching {
                        val json = report.mediaMetadata
                        if (!json.isNullOrBlank()) {
                            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
                            val arr = obj.getAsJsonArray("violationTypes")
                            arr?.mapNotNull { it.asString }?.joinToString(", ")
                        } else null
                    }.getOrNull()
                    append(typesFromMedia ?: report.violationType ?: "OTHERS")
                }
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
    address: String?,
    latitude: Double?,
    longitude: Double?
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
                    val lat = latitude ?: return@clickable
                    val lon = longitude ?: return@clickable
                    val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
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
                    text = address ?: "Location unavailable",
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
fun StatusTimelineSection(report: com.example.reportviolation.data.remote.CitizenReportDetail) {
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
                text = stringResource(R.string.status),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Timeline items
            TimelineItem(
                status = stringResource(R.string.status_received),
                date = report.timestamp,
                isCompleted = true,
                isFirst = true
            )
            
            TimelineItem(
                status = stringResource(R.string.status_submitted_timeline),
                date = report.timestamp,
                isCompleted = true
            )
            
            TimelineItem(
                status = stringResource(R.string.status_in_review),
                date = report.timestamp,
                isCompleted = report.status != null && report.status != com.example.reportviolation.data.model.ReportStatus.PENDING.name,
                isCurrent = report.status == com.example.reportviolation.data.model.ReportStatus.UNDER_REVIEW.name
            )
            
            TimelineItem(
                status = stringResource(R.string.status_resolved_timeline),
                date = if (report.status == com.example.reportviolation.data.model.ReportStatus.APPROVED.name) stringResource(R.string.status_resolved_timeline) else stringResource(R.string.status_pending),
                isCompleted = report.status == com.example.reportviolation.data.model.ReportStatus.APPROVED.name,
                isLast = true,
                isResolved = report.status == com.example.reportviolation.data.model.ReportStatus.APPROVED.name
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
    return com.example.reportviolation.utils.DateTimeUtils.formatForUi(dateTime, "MMM dd, yyyy, h:mm a")
}

