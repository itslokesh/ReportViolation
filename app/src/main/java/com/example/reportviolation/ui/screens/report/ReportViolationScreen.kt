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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CameraPosition
import java.time.LocalDateTime
import com.example.reportviolation.ui.screens.maps.MapsActivity
import android.content.Intent
import android.widget.Toast

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
                    getAddressFromLocation(context, location) { address ->
                        locationAddress = address
                    }
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
                    getAddressFromLocation(context, location) { address ->
                        locationAddress = address
                    }
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
                title = { Text("Report Violation") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Media Capture Section (Only show if location permission granted)
            if (hasLocationPermission) {
                // Location indicator with refresh button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                                                                          Column {
                              Text(
                                  text = if (currentLocation != null) "Location detected" else "Getting location...",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.primary
                              )
                              if (locationAddress != null) {
                                  // Split address by commas and display in multiple lines
                                  val addressParts = locationAddress!!.split(",").map { it.trim() }
                                                                     addressParts.forEachIndexed { _, part ->
                                      if (part.isNotBlank()) {
                                          Text(
                                              text = part,
                                              style = MaterialTheme.typography.bodySmall,
                                              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
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
                                    getAddressFromLocation(context, location) { address ->
                                        locationAddress = address
                                    }
                                }
                                isLocationLoading = false
                            }
                        },
                        enabled = !isLocationLoading
                    ) {
                        if (isLocationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
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

                Spacer(modifier = Modifier.height(24.dp))

                // Media Preview Section
                if (selectedMediaUri != null) {
                    MediaPreviewSection(
                        mediaUri = selectedMediaUri!!,
                        onRemove = { selectedMediaUri = null }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Violation Type Selection
                    ViolationTypeSection(
                        selectedTypes = selectedViolationTypes,
                        onTypeSelected = { selectedViolationTypes = it },
                        onShowDialog = { showViolationTypeDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Location Map Section (Below violation types)
                    if (currentLocation != null) {
                        LocationMapSection(
                            location = currentLocation!!,
                            address = locationAddress,
                            onLocationChanged = { newLocation ->
                                currentLocation = newLocation
                            },
                            onFullMapRequest = {
                                try {
                                    println("Full screen button clicked!")
                                    Toast.makeText(context, "Opening full screen map...", Toast.LENGTH_SHORT).show()
                                    println("Current location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
                                    
                                    val intent = Intent(context, MapsActivity::class.java).apply {
                                        putExtra("latitude", currentLocation!!.latitude)
                                        putExtra("longitude", currentLocation!!.longitude)
                                    }
                                    println("Intent created: $intent")
                                    fullMapLauncher.launch(intent)
                                    println("Activity launcher called")
                                } catch (e: Exception) {
                                    println("Error launching full screen map: ${e.message}")
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Debug info - show when location is not available
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Location Not Available",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Please grant location permission to see the map",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Submit Button
                    val isSubmitEnabled = selectedMediaUri != null && selectedViolationTypes.isNotEmpty() && currentLocation != null
                    
                    Button(
                        onClick = { 
                            // Submit report with all details
                            coroutineScope.launch {
                                                                 submitReport(
                                     mediaUri = selectedMediaUri!!,
                                     violationTypes = selectedViolationTypes,
                                     location = currentLocation!!,
                                     address = locationAddress,
                                     navController = navController,
                                     context = context
                                 )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isSubmitEnabled
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Report")
                    }
                    
                    // Debug info for submit button
                    if (!isSubmitEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Submit Button Requirements:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "• Media: ${if (selectedMediaUri != null) "✓" else "✗"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "• Violation Types: ${if (selectedViolationTypes.isNotEmpty()) "✓" else "✗"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "• Location: ${if (currentLocation != null) "✓" else "✗"}",
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
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Location Access Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We need your location to automatically tag violation reports with GPS coordinates",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Allow Location Access")
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
            text = "Capture Evidence",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Record Video",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Captured Media",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actual media thumbnail with aspect ratio preservation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Show actual media thumbnail with proper aspect ratio
                Image(
                    painter = rememberAsyncImagePainter(
                        model = mediaUri
                    ),
                    contentDescription = "Captured media thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // Play button overlay for videos
                if (mediaUri.endsWith(".mp4") || mediaUri.endsWith(".mov")) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
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
    Column {
        Text(
            text = "Violation Types",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            onClick = onShowDialog,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedTypes.isEmpty()) "Select Violation Types" else "${selectedTypes.size} violation(s) selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedTypes.isNotEmpty()) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (selectedTypes.isNotEmpty()) {
                            Text(
                                text = selectedTypes.joinToString(", ") { it.displayName },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    
                                         Icon(
                         imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                         contentDescription = null,
                         tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
    var selectedTypes by remember { mutableStateOf<Set<ViolationType>>(emptySet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Violation Types")
        },
        text = {
            Column {
                ViolationType.values().forEach { violationType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = violationType.displayName,
                            style = MaterialTheme.typography.bodyMedium
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
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LocationMapSection(
    location: LatLng,
    address: String?,
    onLocationChanged: (LatLng) -> Unit,
    onFullMapRequest: () -> Unit
) {
    // Remember the initial location to prevent marker recreation
    val initialLocation = remember { location }
    Column {
        Text(
            text = "Violation Location",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Move the map to set the exact violation location (pin stays centered)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Google Maps with draggable pin
                GoogleMapWithPin(
                    initialLocation = initialLocation,
                    onLocationChanged = onLocationChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location coordinates display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Lat: ${location.latitude.format(6)}, Lng: ${
                                    location.longitude.format(
                                        6
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            if (address != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 $address",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2
                                )
                            }
                        }

                        // Full Map Button
                        Button(
                            onClick = onFullMapRequest,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Full Screen")
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
    onLocationChanged: (LatLng) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<com.google.android.gms.maps.model.Marker?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }

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
                MapView(context).apply {
                    onCreate(null)
                    mapView = this
                }
            },
            update = { mapView ->
                // Only initialize the map once
                if (!isMapInitialized) {
                    mapView.getMapAsync { googleMap ->
                        // Set map properties
                        googleMap.uiSettings.apply {
                            isZoomControlsEnabled = true
                            isMyLocationButtonEnabled = false
                            isCompassEnabled = true
                            isMapToolbarEnabled = false
                        }

                        // Add marker at initial location (will stay fixed)
                        val markerOptions = MarkerOptions()
                            .position(initialLocation)
                            .title("Violation Location")
                            .snippet("Move map to adjust location")

                        marker = googleMap.addMarker(markerOptions)
                        marker?.isDraggable = false
                        
                        // Move camera to initial location only once
                        val cameraPosition = CameraPosition.Builder()
                            .target(initialLocation)
                            .zoom(15f)
                            .build()
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        
                        // Handle camera movement instead of marker drag (Google Maps-like behavior)
                        googleMap.setOnCameraIdleListener {
                            val centerPosition = googleMap.cameraPosition.target
                            // Update location data but NEVER update marker position
                            println("Camera moved to: ${centerPosition.latitude}, ${centerPosition.longitude}")
                            println("Marker position: ${marker?.position?.latitude}, ${marker?.position?.longitude}")
                            onLocationChanged(centerPosition)
                        }
                        
                        isMapInitialized = true
                    }
                }
            }
        )
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
    context: android.content.Context
) {
    // Create violation report with the first violation type (since the model supports only one)
    val primaryViolationType = violationTypes.firstOrNull() ?: ViolationType.OTHERS

    val report = ViolationReport(
        id = 0, // Room will auto-generate
        reporterId = "current_user", // TODO: Get from auth
        reporterPhone = "1234567890", // TODO: Get from auth
        reporterCity = "Mumbai", // TODO: Get from user profile
        reporterPincode = "400001", // TODO: Get from user profile
        violationType = primaryViolationType,
        severity = SeverityLevel.MAJOR, // TODO: Determine based on violation type
        description = "Reported violations: ${violationTypes.joinToString(", ") { it.displayName }}",
        timestamp = LocalDateTime.now(),
        latitude = location.latitude,
        longitude = location.longitude,
        address = address ?: "Location: ${location.latitude.format(6)}, ${location.longitude.format(6)}",
        pincode = "400001", // TODO: Get from location
        city = "Mumbai", // TODO: Get from location
        district = "Mumbai", // TODO: Get from location
        state = "Maharashtra", // TODO: Get from location
        vehicleNumber = null,
        vehicleType = null,
        vehicleColor = null,
        photoUri = if (mediaUri.endsWith(".jpg") || mediaUri.endsWith(".png")) mediaUri else null,
        videoUri = if (mediaUri.endsWith(".mp4") || mediaUri.endsWith(".mov")) mediaUri else null,
        mediaMetadata = "Multiple violations: ${violationTypes.joinToString(", ") { it.displayName }}"
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

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

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
    onAddressReceived: (String?) -> Unit
) {
    try {
        val geocoder = Geocoder(context)
        
        // Use the newer async API for better compatibility
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressText = buildDetailedAddress(address)
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
                val addressText = buildDetailedAddress(address)
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

private fun buildDetailedAddress(address: Address): String {
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
        val locality = address.locality ?: address.subLocality ?: "Unknown Area"
        val city = address.adminArea ?: "Unknown City"
        "$locality, $city"
    }
}