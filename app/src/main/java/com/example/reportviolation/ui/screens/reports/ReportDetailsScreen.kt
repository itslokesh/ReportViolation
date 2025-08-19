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
import androidx.compose.foundation.layout.statusBarsPadding
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
import java.time.Instant
import com.example.reportviolation.utils.DateTimeUtils
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import com.example.reportviolation.ui.components.MediaPreviewDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.produceState
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner

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
    var events by remember { mutableStateOf<List<com.example.reportviolation.data.remote.ReportEvent>>(emptyList()) }
    
    // Function to load report data
    suspend fun loadReport() {
        try {
            val res = reportsApi.getReport(reportId.toString())
            report = res.data
            // Load events timeline
            val ev = runCatching { reportsApi.getReportEvents(reportId.toString()).data }.getOrNull()
            events = ev ?: emptyList()
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
        navController.navigateUp()
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
    
    // Respect inner padding from root Scaffold (bottom bar)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading && !isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        ,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                report?.let { detail ->
                    ReportDetailsContent(
                        report = detail,
                        events = events
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
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
    events: List<com.example.reportviolation.data.remote.ReportEvent> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header (replaces removed TopAppBar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = stringResource(R.string.report_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Incident Visual Section
        IncidentVisualSection(report = report)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Incident Details Section
        IncidentDetailsSection(report = report)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Timeline Section
        StatusTimelineSection(report = report, events = events)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Review Notes Section
        ReviewNotesSection(report)

        Spacer(modifier = Modifier.height(16.dp))

        // Approved Violation Types (explicit section before rewards)
        ApprovedViolationTypesSection(report)

        Spacer(modifier = Modifier.height(16.dp))

        // Reward Points Section
        RewardPointsSection(report)
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
        var showPreviewDialog by remember { mutableStateOf(false) }
        val mediaUri = report.photoUrl ?: report.videoUrl

        if (showPreviewDialog && !mediaUri.isNullOrBlank()) {
            MediaPreviewDialog(
                mediaUri = mediaUri,
                onDismiss = { showPreviewDialog = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF1976D2))
                .clickable(enabled = !mediaUri.isNullOrBlank()) { showPreviewDialog = true }
        ) {
            if (report.photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(report.photoUrl),
                    contentDescription = "Incident Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (report.videoUrl != null) {
                val videoUrl = report.videoUrl
                val thumbnail = produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, videoUrl) {
                    value = withContext(Dispatchers.IO) {
                        runCatching {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(videoUrl, HashMap())
                            val frame = retriever.getFrameAtTime(1_000_000)
                            retriever.release()
                            frame?.asImageBitmap()
                        }.getOrNull()
                    }
                }.value

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail,
                            contentDescription = "Video thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
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
                value = run {
                    val ctx = LocalContext.current
                    val typesFromMedia = runCatching {
                        val json = report.mediaMetadata
                        if (!json.isNullOrBlank()) {
                            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
                            val arr = obj.getAsJsonArray("violationTypes")
                            arr?.mapNotNull { it.asString }
                        } else null
                    }.getOrNull()
                    val primary = (typesFromMedia?.firstOrNull() ?: report.violationType ?: "OTHERS")
                    if (typesFromMedia != null && typesFromMedia.isNotEmpty()) {
                        typesFromMedia.joinToString(", ") { getLocalizedViolationTypeName(com.example.reportviolation.data.model.ViolationType.valueOf(it), ctx) }
                    } else {
                        getLocalizedViolationTypeName(com.example.reportviolation.data.model.ViolationType.valueOf(primary), ctx)
                    }
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
fun StatusTimelineSection(
    report: com.example.reportviolation.data.remote.CitizenReportDetail,
    events: List<com.example.reportviolation.data.remote.ReportEvent> = emptyList()
) {
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
            
            val timelineEvents = events.filter { type ->
                val t = (type.type ?: "").uppercase()
                t == "STATUS_UPDATED" || t == "POINTS_AWARDED"
            }
            if (timelineEvents.isNotEmpty()) {
                timelineEvents.sortedBy { it.createdAt }.forEachIndexed { index, ev ->
                    val statusFromMeta = runCatching {
                        val meta = ev.metadata
                        if (!meta.isNullOrBlank()) {
                            val obj = com.google.gson.JsonParser.parseString(meta).asJsonObject
                            obj.get("status")?.asString
                        } else null
                    }.getOrNull()
                    val statusKey = statusFromMeta ?: ev.status
                    val label = if ((ev.type ?: "").equals("POINTS_AWARDED", ignoreCase = true)) {
                        stringResource(R.string.status_points_rewarded)
                    } else when (statusKey) {
                        com.example.reportviolation.data.model.ReportStatus.PENDING.name -> stringResource(R.string.status_submitted_timeline)
                        com.example.reportviolation.data.model.ReportStatus.UNDER_REVIEW.name -> stringResource(R.string.status_in_review)
                        com.example.reportviolation.data.model.ReportStatus.APPROVED.name -> stringResource(R.string.status_report_approved)
                        com.example.reportviolation.data.model.ReportStatus.REJECTED.name -> stringResource(R.string.status_rejected)
                        else -> {
                            // Map some common raw keys to human labels
                            when ((statusKey ?: ev.type ?: "").lowercase()) {
                                "submitted", "submission", "received" -> stringResource(R.string.status_submitted_timeline)
                                "under_review", "in_review", "review" -> stringResource(R.string.status_in_review)
                                "approved", "resolved" -> stringResource(R.string.status_report_approved)
                                "rejected", "declined" -> stringResource(R.string.status_rejected)
                                else -> (statusKey ?: ev.type ?: "")
                            }
                        }
                    }
                    val isRejected = (statusKey == com.example.reportviolation.data.model.ReportStatus.REJECTED.name)
                    TimelineItem(
                        status = label,
                        date = formatEventRelativeIst(ev.createdAt),
                        isCompleted = true,
                        isFirst = index == 0,
                        isLast = index == timelineEvents.lastIndex,
                        isCurrent = (statusKey == com.example.reportviolation.data.model.ReportStatus.UNDER_REVIEW.name),
                        isResolved = (statusKey == com.example.reportviolation.data.model.ReportStatus.APPROVED.name),
                        isRejected = isRejected
                    )
                }
            } else {
                // Fallback static timeline
                TimelineItem(
                    status = stringResource(R.string.status_received),
                    date = formatEventRelativeIst(report.timestamp),
                    isCompleted = true,
                    isFirst = true
                )
                TimelineItem(
                    status = stringResource(R.string.status_submitted_timeline),
                    date = formatEventRelativeIst(report.timestamp),
                    isCompleted = true
                )
                TimelineItem(
                    status = stringResource(R.string.status_in_review),
                    date = formatEventRelativeIst(report.timestamp),
                    isCompleted = report.status != null && report.status != com.example.reportviolation.data.model.ReportStatus.PENDING.name,
                    isCurrent = report.status == com.example.reportviolation.data.model.ReportStatus.UNDER_REVIEW.name
                )
                TimelineItem(
                    status = stringResource(R.string.status_report_approved),
                    date = formatEventRelativeIst(report.updatedAt ?: report.timestamp),
                    isCompleted = report.status == com.example.reportviolation.data.model.ReportStatus.APPROVED.name,
                    isLast = true,
                    isResolved = report.status == com.example.reportviolation.data.model.ReportStatus.APPROVED.name,
                    isRejected = report.status == com.example.reportviolation.data.model.ReportStatus.REJECTED.name
                )
            }
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
    isResolved: Boolean = false,
    isRejected: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline line and icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
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
                        if (isRejected) {
                            Color(0xFFE53935)
                        } else if (isResolved) {
                            Color(0xFF4CAF50)
                        } else if (isCurrent || isCompleted) {
                            Color(0xFF1976D2)
                        } else {
                            Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = if (isRejected) {
                    Icons.Default.Close
                } else if (isResolved) {
                    Icons.Default.Check
                } else if (isCurrent) {
                    Icons.Default.Refresh
                } else if (isCompleted) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Schedule
                }
                Icon(
                    imageVector = iconVector,
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
        
        // Status details aligned with icon center
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isRejected) {
                    Color(0xFFE53935)
                } else if (isResolved) {
                    Color(0xFF4CAF50)
                } else if (isCurrent) {
                    Color(0xFF1976D2)
                } else if (isCompleted) {
                    Color.Black
                } else {
                    Color.Gray
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

@Composable
private fun formatEventRelativeIst(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    val instant = runCatching {
        try { Instant.parse(createdAt) } catch (_: Exception) {
            try { java.time.OffsetDateTime.parse(createdAt).toInstant() } catch (_: Exception) {
                java.time.ZonedDateTime.parse(createdAt).toInstant()
            }
        }
    }.getOrNull() ?: return ""

    val nowIst = DateTimeUtils.nowZonedIst()
    val timeIst = instant.atZone(DateTimeUtils.IST)
    val duration = java.time.Duration.between(timeIst, nowIst)
    val seconds = duration.seconds
    val minutes = duration.toMinutes()
    return when {
        seconds < 60 && seconds >= 0 -> stringResource(R.string.time_just_now)
        minutes in 1..59 -> stringResource(R.string.time_minutes_ago, minutes)
        timeIst.toLocalDate().isEqual(nowIst.toLocalDate()) ->
            stringResource(R.string.time_today_at, DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a"))
        timeIst.toLocalDate().plusDays(1).isEqual(nowIst.toLocalDate()) ->
            stringResource(R.string.time_yesterday_at, DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "hh:mm a"))
        timeIst.year == nowIst.year -> DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM, hh:mm a")
        else -> DateTimeUtils.formatForUi(timeIst.toLocalDateTime(), pattern = "dd MMM yyyy, hh:mm a")
    }
}

@Composable
fun ReviewNotesSection(report: CitizenReportDetail) {
    val notes = report.reviewNotes
    if (notes.isNullOrBlank()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.review_notes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RewardPointsSection(report: CitizenReportDetail) {
    val isApproved = report.status == ReportStatus.APPROVED.name
    val points = report.pointsAwarded ?: 0
    if (!isApproved || points <= 0) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.rewards_points_earned),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ApprovedViolationsSection(report: CitizenReportDetail) {
    if (report.status != ReportStatus.APPROVED.name) return
    val items: List<String> = runCatching {
        val json = report.mediaMetadata
        if (!json.isNullOrBlank()) {
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
            obj.getAsJsonArray("approvedViolationTypes")?.mapNotNull { it.asString } ?: emptyList()
        } else emptyList()
    }.getOrDefault(emptyList())
    if (items.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.approved_violations),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val ctx = LocalContext.current
            items.forEachIndexed { index, code ->
                val friendly = runCatching { com.example.reportviolation.data.model.ViolationType.valueOf(code) }
                    .getOrNull()
                    ?.let { getLocalizedViolationTypeName(it, ctx) }
                    ?: code.replace('_', ' ').lowercase().split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
                Text(
                    text = "• " + friendly,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (index != items.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun ApprovedViolationTypesSection(report: CitizenReportDetail) {
    // Parse approvedViolationTypes from mediaMetadata JSON; fallback to single violationType if status is APPROVED
    val itemsFromMedia: List<String> = runCatching {
        val json = report.mediaMetadata
        if (!json.isNullOrBlank()) {
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
            obj.getAsJsonArray("approvedViolationTypes")?.mapNotNull { it.asString } ?: emptyList()
        } else emptyList()
    }.getOrDefault(emptyList())
    val items: List<String> = if (itemsFromMedia.isNotEmpty()) itemsFromMedia else {
        if (report.status == ReportStatus.APPROVED.name && !report.violationType.isNullOrBlank()) listOf(report.violationType!!) else emptyList()
    }
    if (items.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.approved_violation_types),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val ctx = LocalContext.current
            items.forEachIndexed { index, code ->
                val friendly = runCatching { com.example.reportviolation.data.model.ViolationType.valueOf(code) }
                    .getOrNull()
                    ?.let { getLocalizedViolationTypeName(it, ctx) }
                    ?: code.replace('_', ' ').lowercase().split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
                Text(
                    text = "• " + friendly,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (index != items.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

