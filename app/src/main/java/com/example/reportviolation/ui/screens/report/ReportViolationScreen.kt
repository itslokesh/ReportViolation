package com.example.reportviolation.ui.screens.report

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.reportviolation.data.model.ViolationType
import com.example.reportviolation.data.model.ViolationReport
import com.example.reportviolation.data.model.ReportStatus
import com.example.reportviolation.data.model.SeverityLevel
import com.example.reportviolation.data.repository.ViolationReportRepository
import com.example.reportviolation.data.local.AppDatabase
import com.example.reportviolation.domain.service.DuplicateDetectionService
import com.example.reportviolation.domain.service.JurisdictionService
import com.example.reportviolation.ui.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import android.location.Geocoder
import android.location.Address
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import java.time.LocalDateTime
import com.example.reportviolation.ui.screens.maps.MapsActivity
import android.content.Intent
import android.widget.Toast
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.reportviolation.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.reportviolation.ui.components.MediaPreviewDialog
import com.example.reportviolation.ui.components.ViolationIcon
import com.example.reportviolation.ui.components.ViolationIconDisplayMode
import com.example.reportviolation.utils.getLocalizedViolationTypeName
import com.example.reportviolation.ui.theme.DarkBlue
import com.example.reportviolation.ui.theme.MediumBlue
import com.example.reportviolation.ui.theme.LightBlue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

// Custom MapView that restricts one-finger dragging
class RestrictedMapView(context: android.content.Context) : MapView(context) {
    private var touchCount = 0
    private var isDragging = false
    private var lastX = 0f
    private var lastY = 0f
    private val dragThreshold = 10f // Minimum distance to consider as drag
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchCount = 1
                isDragging = false
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                touchCount++
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchCount == 1) {
                    val deltaX = Math.abs(event.x - lastX)
                    val deltaY = Math.abs(event.y - lastY)
                    
                    if (deltaX > dragThreshold || deltaY > dragThreshold) {
                        isDragging = true
                        // For now, we'll allow the drag but provide visual feedback
                        // A more sophisticated implementation would require custom gesture detection
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                touchCount--
            }
            MotionEvent.ACTION_UP -> {
                touchCount = 0
                isDragging = false
            }
        }
        
        // Allow all gestures for now, but provide clear visual feedback
        // The marker behavior and instructions will guide users to use two fingers
        return super.onTouchEvent(event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViolationScreen(navController: NavController) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var selectedViolationTypes by remember { mutableStateOf<Set<ViolationType>>(emptySet()) }
    var showViolationTypeDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationAddress by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Activity result launcher for full-screen map
    val fullMapLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        println("Activity result received: ${result.resultCode}")
        println("Activity result data: ${result.data}")
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                println("Activity result OK")
                result.data?.let { data ->
                    val latitude = data.getDoubleExtra("selected_latitude", 0.0)
                    val longitude = data.getDoubleExtra("selected_longitude", 0.0)
                    val address = data.getStringExtra("selected_address")
                    
                    println("Received location: $latitude, $longitude")
                    println("Received address: $address")
                    
                    if (latitude != 0.0 && longitude != 0.0) {
                        currentLocation = LatLng(latitude, longitude)
                        locationAddress = address
                        println("Location updated successfully")
                    }
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                // User cancelled or there was an error, keep current location
                println("Full screen map was cancelled or encountered an error")
            }
        }
    }

    // Check initial permission status and get location if already granted
    // Get localized strings
    val unknownArea = stringResource(R.string.unknown_area)
    val unknownCity = stringResource(R.string.unknown_city)
    
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        // If permission is already granted, get current location
        if (hasLocationPermission && currentLocation == null) {
            isLocationLoading = true
            getCurrentLocation(context) { location ->
                currentLocation = location
                if (location != null) {
                    getAddressFromLocation(
                        context, 
                        location, 
                        { address -> locationAddress = address },
                        unknownArea,
                        unknownCity
                    )
                }
                isLocationLoading = false
            }
        }
    }

    // Listen for captured media from camera screen
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("capturedMediaUri", null)
            ?.collect { uri ->
                uri?.let { selectedMediaUri = it }
            }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            // Get real GPS location when permission is granted
            getCurrentLocation(context) { location ->
                currentLocation = location
                if (location != null) {
                    getAddressFromLocation(
                        context, 
                        location, 
                        { address -> locationAddress = address },
                        unknownArea,
                        unknownCity
                    )
                }
                isLocationLoading = false
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Navigate to camera screen
            navController.navigate(Screen.Camera.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_violation_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Automatic Location Detection (Clean and transparent)
            if (!hasLocationPermission) {
                LocationPermissionRequest(
                    onRequestPermission = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Media Capture Section (Only show if location permission granted)
            if (hasLocationPermission) {
                                // Location indicator with refresh button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = DarkBlue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentLocation != null) stringResource(R.string.location_detected) else stringResource(R.string.getting_location),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                                if (locationAddress != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Split address by commas and display in multiple lines
                                    val addressParts = locationAddress!!.split(",").map { it.trim() }
                                    addressParts.forEachIndexed { _, part ->
                                        if (part.isNotBlank()) {
                                            Text(
                                                text = part,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Refresh location button
                        IconButton(
                            onClick = { 
                                isLocationLoading = true
                                getCurrentLocation(context) { location ->
                                    currentLocation = location
                                    if (location != null) {
                                        getAddressFromLocation(
                                            context, 
                                            location, 
                                            { address -> locationAddress = address },
                                            unknownArea,
                                            unknownCity
                                        )
                                    }
                                    isLocationLoading = false
                                }
                            },
                            enabled = !isLocationLoading
                        ) {
                            if (isLocationLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = DarkBlue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.refresh_location),
                                    tint = DarkBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                MediaCaptureSection(
                    selectedMediaUri = selectedMediaUri,
                    onPhotoClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            android.util.Log.d("ReportViolationScreen", "Navigating to camera screen with PHOTO mode")
                            navController.navigate("${Screen.Camera.route}?mode=PHOTO")
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onVideoClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            android.util.Log.d("ReportViolationScreen", "Navigating to camera screen with VIDEO mode")
                            navController.navigate("${Screen.Camera.route}?mode=VIDEO")
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Media Preview Section
                if (selectedMediaUri != null) {
                    MediaPreviewSection(
                        mediaUri = selectedMediaUri!!,
                        onRemove = { selectedMediaUri = null }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Violation Type Selection
                    ViolationTypeSection(
                        selectedTypes = selectedViolationTypes,
                        onTypeSelected = { selectedViolationTypes = it },
                        onShowDialog = { showViolationTypeDialog = true }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Location Map Section (Below violation types)
                    if (currentLocation != null) {
                        LocationMapSection(
                            location = currentLocation!!,
                            address = locationAddress,
                            onLocationChanged = { newLocation ->
                                currentLocation = newLocation
                            },
                            onAddressChanged = { newAddress ->
                                locationAddress = newAddress
                            },
                            onFullMapRequest = {
                                try {
                                    println("Full screen button clicked!")
                                    Toast.makeText(context, "Opening full screen map...", Toast.LENGTH_SHORT).show()
                                    println("Current location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
                                    
                                    val intent = Intent(context, MapsActivity::class.java).apply {
                                        putExtra("latitude", currentLocation!!.latitude)
                                        putExtra("longitude", currentLocation!!.longitude)
                                        putExtra("address", locationAddress)
                                    }
                                    println("Intent created: $intent")
                                    println("Intent extras: ${intent.extras}")
                                    fullMapLauncher.launch(intent)
                                    println("Activity launcher called")
                                } catch (e: Exception) {
                                    println("Error launching full screen map: ${e.message}")
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            },
                            unknownArea = unknownArea,
                            unknownCity = unknownCity
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    } else {
                        // Debug info - show when location is not available
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.location_not_available),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = stringResource(R.string.grant_location_permission),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                                        // Submit Button
                    val isSubmitEnabled = selectedMediaUri != null && selectedViolationTypes.isNotEmpty() && currentLocation != null
                    
                    Button(
                        onClick = { 
                            // Submit to backend using required payload shape
                            coroutineScope.launch {
                                try {
                                    val parts = resolveAddressParts(context, currentLocation!!, locationAddress)
                                    val ok = ReportBackendBridge.createReport(
                                        context = context,
                                        selectedMediaUri = selectedMediaUri,
                                        violationTypes = selectedViolationTypes.map { it.name },
                                        severity = null,
                                        description = "Reported via Android app",
                                        latitude = currentLocation!!.latitude,
                                        longitude = currentLocation!!.longitude,
                                        address = parts.address,
                                        pincode = parts.pincode,
                                        city = parts.city,
                                        district = parts.district,
                                        state = parts.state,
                                        isAnonymous = false,
                                        vehicleNumber = null,
                                        vehicleType = null,
                                        vehicleColor = null,
                                    )
                                    if (ok) {
                                        navController.navigate(Screen.ReportsHistory.route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("REPORT_SUBMIT_FAILED error=${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isSubmitEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.submit_report),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Debug info for submit button
                    if (!isSubmitEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.submit_button_requirements),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• ${stringResource(R.string.evidence)}: ${if (selectedMediaUri != null) "✓" else "✗"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "• ${stringResource(R.string.violation_types)}: ${if (selectedViolationTypes.isNotEmpty()) "✓" else "✗"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "• ${stringResource(R.string.location)}: ${if (currentLocation != null) "✓" else "✗"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Violation Type Selection Dialog
        if (showViolationTypeDialog) {
            ViolationTypeDialog(
                onDismiss = { showViolationTypeDialog = false },
                onTypeSelected = { types ->
                    selectedViolationTypes = types
                    showViolationTypeDialog = false
                }
            )
        }

        // Full Map Dialog removed - map is now integrated directly
    }
}

@Composable
fun LocationPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = DarkBlue
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.location_access_required),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.location_permission_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    stringResource(R.string.allow_location_access),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MediaCaptureSection(
    selectedMediaUri: String?,
    onPhotoClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    Column {
                    Text(
                text = stringResource(R.string.capture_evidence),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo Capture Button
            Card(
                onClick = onPhotoClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.photo),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = DarkBlue
                    )
                }
            }
            
            // Video Capture Button
            Card(
                onClick = onVideoClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MediumBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.record_video),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MediumBlue
                    )
                }
            }
        }
    }
}

@Composable
fun MediaPreviewSection(
    mediaUri: String,
    onRemove: () -> Unit
) {
    var showPreviewDialog by remember { mutableStateOf(false) }
    
    val isVideo = mediaUri.endsWith(".mp4") || mediaUri.endsWith(".mov") || mediaUri.contains("video")
    
    // Media preview dialog
    if (showPreviewDialog) {
        MediaPreviewDialog(
            mediaUri = mediaUri,
            onDismiss = { showPreviewDialog = false }
        )
    }
    
        Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                            Text(
                text = if (isVideo) stringResource(R.string.video) else stringResource(R.string.photo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_media),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Media thumbnail with aspect ratio preservation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showPreviewDialog = true },
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Show image (photo)
                    !isVideo -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = mediaUri
                            ),
                            contentDescription = stringResource(R.string.photo_thumbnail),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    // Show video placeholder with play button
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = stringResource(R.string.video_media),
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.video_media),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Play button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.play_video),
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViolationTypeSection(
    selectedTypes: Set<ViolationType>,
    onTypeSelected: (Set<ViolationType>) -> Unit,
    onShowDialog: () -> Unit
) {
    val context = LocalContext.current
    Column {
        Text(
            text = stringResource(R.string.violation_types_section),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            onClick = onShowDialog,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = DarkBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedTypes.isEmpty()) stringResource(R.string.select_violation_type) else "${selectedTypes.size} ${stringResource(R.string.violation_selected)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTypes.isNotEmpty()) 
                                DarkBlue
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (selectedTypes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedTypes.joinToString(", ") { getLocalizedViolationTypeName(it, context) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = DarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ViolationTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (Set<ViolationType>) -> Unit
) {
    val context = LocalContext.current
    var selectedTypes by remember { mutableStateOf<Set<ViolationType>>(emptySet()) }
    
        AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.select_violation_type),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        },
        text = {
            Column {
                ViolationType.values().forEach { violationType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val isSelected = selectedTypes.contains(violationType)
                                selectedTypes = if (isSelected) selectedTypes - violationType else selectedTypes + violationType
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedTypes.contains(violationType),
                            onCheckedChange = { checked ->
                                selectedTypes = if (checked) {
                                    selectedTypes + violationType
                                } else {
                                    selectedTypes - violationType
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DarkBlue,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        ViolationIcon(
                            violationType = violationType,
                            displayMode = ViolationIconDisplayMode.SELECTION,
                            isSelected = selectedTypes.contains(violationType)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = getLocalizedViolationTypeName(violationType, context),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTypeSelected(selectedTypes) },
                enabled = selectedTypes.isNotEmpty()
            ) {
                Text(
                    stringResource(R.string.confirm_selection),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DarkBlue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel_selection),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun LocationMapSection(
    location: LatLng,
    address: String?,
    onLocationChanged: (LatLng) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onFullMapRequest: () -> Unit,
    unknownArea: String,
    unknownCity: String
) {
    // Use the current location directly to ensure synchronization
    val currentLocation = remember { mutableStateOf(location) }
    
    // Update currentLocation when location prop changes
    LaunchedEffect(location) {
        currentLocation.value = location
    }
        Column {
        Text(
            text = stringResource(R.string.violation_location),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.use_two_fingers_set_location),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Google Maps with fixed pin
                GoogleMapWithPin(
                    initialLocation = currentLocation.value,
                    onLocationChanged = onLocationChanged,
                    onAddressChanged = onAddressChanged,
                    unknownArea = unknownArea,
                    unknownCity = unknownCity
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Location coordinates display
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DarkBlue,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.latitude_longitude_format,
                                    currentLocation.value.latitude,
                                    currentLocation.value.longitude
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            if (address != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 $address",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkBlue,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Add explanation about the fixed pin behavior
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.red_pin_fixed_center),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.use_two_fingers_adjust_location),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            
                            // Full Map Button
                            Button(
                                onClick = onFullMapRequest,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MediumBlue,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.full_screen),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
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
fun GoogleMapWithPin(
    initialLocation: LatLng,
    onLocationChanged: (LatLng) -> Unit,
    onAddressChanged: (String?) -> Unit,
    unknownArea: String,
    unknownCity: String
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<RestrictedMapView?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf(initialLocation) }
    var mapInstance by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        AndroidView(
            factory = { context ->
                RestrictedMapView(context).apply {
                    onCreate(null)
                    mapView = this
                }
            },
            update = { mapView ->
                // Only initialize the map once
                if (!isMapInitialized) {
                    mapView.getMapAsync { googleMap ->
                        // Set map properties for embed map
                        googleMap.uiSettings.apply {
                            isZoomControlsEnabled = true
                            isMyLocationButtonEnabled = false
                            isCompassEnabled = true
                            isMapToolbarEnabled = false
                        }

                        // Disable map interactions that could interfere with marker positioning
                        googleMap.setOnMapClickListener { /* Disable single tap */ }
                        googleMap.setOnMapLongClickListener { /* Disable long press */ }

                        // No Google Maps marker needed - we'll use our custom fixed overlay instead
                        // This ensures the marker stays visually fixed in the center of the widget
                        
                        // Move camera to initial location
                        val cameraPosition = CameraPosition.Builder()
                            .target(initialLocation)
                            .zoom(15f)
                            .build()
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        
                        // Handle camera movement - marker stays fixed, location updates based on center
                        googleMap.setOnCameraIdleListener {
                            val centerPosition = googleMap.cameraPosition.target
                            currentLocation = centerPosition
                            
                            // DO NOT update marker position - keep it visually fixed in center
                            // The marker will appear to stay in center because we're using the center coordinates
                            println("Camera moved to: ${centerPosition.latitude}, ${centerPosition.longitude}")
                            println("Marker appears fixed at center")
                            onLocationChanged(centerPosition)
                            
                            // Update address when location changes
                            getAddressFromLocation(
                                context, 
                                centerPosition, 
                                { address -> 
                                    onAddressChanged(address)
                                    println("New address: $address")
                                },
                                unknownArea,
                                unknownCity
                            )
                        }
                        
                        // Store the googleMap instance for later use
                        mapInstance = googleMap
                        isMapInitialized = true
                    }
                }
            }
        )
        
        // Update map camera when initialLocation changes (e.g., returning from full-screen)
        LaunchedEffect(initialLocation) {
            if (isMapInitialized && mapInstance != null) {
                val cameraPosition = CameraPosition.Builder()
                    .target(initialLocation)
                    .zoom(15f)
                    .build()
                mapInstance?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                currentLocation = initialLocation
            }
        }
        
        // Add a visual indicator that the marker is fixed and instructions
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "📍 Pin stays centered",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.use_two_fingers),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Add a custom fixed marker overlay that stays in the center
        // Using the same style as the full-screen marker
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(32.dp)
        ) {
            // Use the same location icon as full-screen
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.reportviolation.R.drawable.ic_location_on),
                                        contentDescription = stringResource(R.string.fixed_location_marker),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

// FullMapDialog removed - map is now integrated directly

private suspend fun submitReport(
    mediaUri: String,
    violationTypes: Set<ViolationType>,
    location: LatLng,
    address: String?,
    navController: NavController,
    context: android.content.Context,
    unknownCity: String
) {
    // Create violation report with the first violation type (since the model supports only one)
    val primaryViolationType = violationTypes.firstOrNull() ?: ViolationType.OTHERS

    val report = ViolationReport(
        id = 0, // Room will auto-generate
        reporterId = "current_user", // TODO: Get from auth
        reporterPhone = "1234567890", // TODO: Get from auth
        reporterCity = unknownCity, // TODO: Get from user profile
        reporterPincode = "400001", // TODO: Get from user profile
        violationType = primaryViolationType,
        severity = SeverityLevel.MAJOR, // TODO: Determine based on violation type
        description = "Reported violations: ${violationTypes.joinToString(", ") { getLocalizedViolationTypeName(it, context) }}",
        timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime(),
        latitude = location.latitude,
        longitude = location.longitude,
        address = address ?: "Location: ${location.latitude.format(6)}, ${location.longitude.format(6)}",
        pincode = "400001", // TODO: Get from location
        city = unknownCity, // TODO: Get from location
        district = unknownCity, // TODO: Get from location
        state = unknownCity, // TODO: Get from location
        vehicleNumber = null,
        vehicleType = null,
        vehicleColor = null,
        photoUri = if (mediaUri.endsWith(".jpg") || mediaUri.endsWith(".png")) mediaUri else null,
        videoUri = if (mediaUri.endsWith(".mp4") || mediaUri.endsWith(".mov")) mediaUri else null,
        mediaMetadata = "Multiple violations: ${violationTypes.joinToString(", ") { getLocalizedViolationTypeName(it, context) }}"
    )

    // Save to database using repository
    val database = AppDatabase.getDatabase(context)
    val duplicateDetectionService = DuplicateDetectionService()
    val jurisdictionService = JurisdictionService()
    val repository = ViolationReportRepository(
        database.violationReportDao(),
        duplicateDetectionService,
        jurisdictionService
    )

    // Save the report to database
    try {
        repository.createReport(report)
        println("Report saved successfully: $report")
    } catch (e: Exception) {
        println("Failed to save report: ${e.message}")
    }

    // Navigate to reports page to show submissions
    navController.navigate(Screen.ReportsHistory.route) {
        popUpTo(Screen.Dashboard.route) { inclusive = false }
    }
}

// Mirror a successful backend submission into local Room so previous datastore remains in sync.
private suspend fun saveLocalReportMirror(
    context: android.content.Context,
    mediaUri: String,
    uploadedPhotoUrl: String?,
    uploadedVideoUrl: String?,
    violationTypes: Set<com.example.reportviolation.data.model.ViolationType>,
    location: com.google.android.gms.maps.model.LatLng,
    address: String?,
    unknownCity: String,
) {
    val database = com.example.reportviolation.data.local.AppDatabase.getDatabase(context)
    val duplicateDetectionService = com.example.reportviolation.domain.service.DuplicateDetectionService()
    val jurisdictionService = com.example.reportviolation.domain.service.JurisdictionService()
    val repository = com.example.reportviolation.data.repository.ViolationReportRepository(
        database.violationReportDao(),
        duplicateDetectionService,
        jurisdictionService
    )

    val primaryViolationType = violationTypes.firstOrNull() ?: com.example.reportviolation.data.model.ViolationType.OTHERS
    val report = com.example.reportviolation.data.model.ViolationReport(
        reporterId = "current_user",
        reporterPhone = "",
        reporterCity = unknownCity,
        reporterPincode = "",
        violationType = primaryViolationType,
        severity = com.example.reportviolation.data.model.SeverityLevel.MAJOR,
        description = "(Mirrored) Reported violations: ${violationTypes.joinToString(", ") { it.displayName }}",
        timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime(),
        latitude = location.latitude,
        longitude = location.longitude,
        address = address ?: "",
        pincode = "",
        city = unknownCity,
        district = unknownCity,
        state = unknownCity,
        vehicleNumber = null,
        vehicleType = null,
        vehicleColor = null,
        photoUri = uploadedPhotoUrl ?: if (!mediaUri.endsWith(".mp4") && !mediaUri.contains("video")) mediaUri else null,
        videoUri = uploadedVideoUrl ?: if (mediaUri.endsWith(".mp4") || mediaUri.contains("video")) mediaUri else null,
        mediaMetadata = null,
        status = com.example.reportviolation.data.model.ReportStatus.PENDING,
        isDuplicate = false,
        duplicateGroupId = null,
        confidenceScore = null,
        reviewerId = null,
        reviewTimestamp = null,
        reviewNotes = null,
        challanIssued = false,
        challanNumber = null,
        pointsAwarded = 0,
        isFirstReporter = false,
        createdAt = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime(),
        updatedAt = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toLocalDateTime(),
        isAnonymous = false
    )

    try {
        repository.createReport(report)
    } catch (_: Exception) {
        // Best-effort mirror; ignore failures
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

private data class AddressParts(
    val address: String,
    val pincode: String,
    val city: String,
    val district: String,
    val state: String,
)

private fun resolveAddressParts(
    context: android.content.Context,
    latLng: LatLng,
    fallbackAddress: String?
): AddressParts {
    val geocoder = Geocoder(context)
    return try {
        val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        val a = list?.firstOrNull()
        val addressLine = when {
            !fallbackAddress.isNullOrBlank() -> fallbackAddress
            a?.getAddressLine(0)?.length ?: 0 >= 5 -> a?.getAddressLine(0) ?: "Unknown"
            else -> listOfNotNull(a?.subLocality, a?.thoroughfare, a?.locality, a?.adminArea)
                .filter { !it.isNullOrBlank() }
                .joinToString(", ")
        }
        AddressParts(
            address = addressLine.take(200).padEnd(5, ' ').trim(),
            pincode = (a?.postalCode ?: "000000").padEnd(6, '0').take(6),
            city = (a?.locality ?: a?.adminArea ?: "NA").take(50).padEnd(2, ' ').trim(),
            district = (a?.subAdminArea ?: a?.locality ?: "NA").take(50).padEnd(2, ' ').trim(),
            state = (a?.adminArea ?: "NA").take(50).padEnd(2, ' ').trim(),
        )
    } catch (_: Exception) {
        AddressParts(
            address = (fallbackAddress ?: "Location: ${latLng.latitude.format(6)}, ${latLng.longitude.format(6)}").take(200).padEnd(5, ' ').trim(),
            pincode = "000000",
            city = "NA",
            district = "NA",
            state = "NA",
        )
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (LatLng?) -> Unit
) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }
            ).addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    println("Location obtained: ${it.latitude}, ${it.longitude}")
                    onLocationReceived(latLng)
                } ?: run {
                    println("Location is null")
                    onLocationReceived(null)
                }
            }.addOnFailureListener { exception ->
                println("Failed to get location: ${exception.message}")
                // Fallback to a default location if GPS fails
                onLocationReceived(LatLng(19.0760, 72.8777)) // Mumbai as fallback
            }
        } else {
            println("Location permission not granted")
            onLocationReceived(LatLng(19.0760, 72.8777)) // Mumbai as fallback
        }
    } catch (e: Exception) {
        println("Error getting location: ${e.message}")
        onLocationReceived(LatLng(19.0760, 72.8777)) // Mumbai as fallback
    }
}

private fun getAddressFromLocation(
    context: android.content.Context,
    location: LatLng,
    onAddressReceived: (String?) -> Unit,
    unknownArea: String,
    unknownCity: String
) {
    try {
        val geocoder = Geocoder(context)
        
        // Use the newer async API for better compatibility
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressText = buildDetailedAddress(address, unknownArea, unknownCity)
                    println("Detailed address obtained: $addressText")
                    onAddressReceived(addressText)
                } else {
                    println("No address found for location")
                    onAddressReceived(null)
                }
            }
        } else {
            // Fallback for older API levels
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressText = buildDetailedAddress(address, unknownArea, unknownCity)
                println("Detailed address obtained: $addressText")
                onAddressReceived(addressText)
            } else {
                println("No address found for location")
                onAddressReceived(null)
            }
        }
    } catch (e: Exception) {
        println("Error getting address: ${e.message}")
        onAddressReceived(null)
    }
}

private fun buildDetailedAddress(address: Address, unknownArea: String, unknownCity: String): String {
    // Build detailed address with multiple components
    val addressParts = mutableListOf<String>()

    // Add sub-locality (neighborhood/area) if available
    address.subLocality?.let {
        if (it.isNotBlank()) addressParts.add(it)
    }

    // Add thoroughfare (street name) if available
    address.thoroughfare?.let {
        if (it.isNotBlank()) addressParts.add(it)
    }

    // Add sub-thoroughfare (landmark/building) if available
    address.subThoroughfare?.let {
        if (it.isNotBlank()) addressParts.add(it)
    }

    // Add locality (city district) if available
    address.locality?.let {
        if (it.isNotBlank() && !addressParts.contains(it)) addressParts.add(it)
    }

    // Add admin area (city) if available
    address.adminArea?.let {
        if (it.isNotBlank() && !addressParts.contains(it)) addressParts.add(it)
    }

    // If we have detailed parts, use them; otherwise fallback to basic locality
    return if (addressParts.isNotEmpty()) {
        addressParts.joinToString(", ")
    } else {
        val locality = address.locality ?: address.subLocality ?: unknownArea
        val city = address.adminArea ?: unknownCity
        "$locality, $city"
    }
}