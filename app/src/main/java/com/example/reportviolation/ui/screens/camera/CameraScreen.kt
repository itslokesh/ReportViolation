package com.example.reportviolation.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    mode: String = "PHOTO",
    viewModel: CameraViewModel = viewModel()
) {
    // Get the camera mode from navigation argument
    val initialMode = if (mode == "VIDEO") CameraMode.VIDEO else CameraMode.PHOTO
    
    // Debug logging
    android.util.Log.d("CameraScreen", "=== CAMERA SCREEN INITIALIZATION ===")
    android.util.Log.d("CameraScreen", "Mode from navigation argument: '$mode'")
    android.util.Log.d("CameraScreen", "Initial mode set to: $initialMode")
    android.util.Log.d("CameraScreen", "=== END CAMERA SCREEN INITIALIZATION ===")
    

    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasMicrophonePermission by remember { mutableStateOf(false) }
    var capturedMediaUri by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var recordingStartTime by remember { mutableStateOf(0L) }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Check initial permissions
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        hasMicrophonePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Microphone permission launcher
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicrophonePermission = isGranted
    }
    
    // Request camera permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Initialize camera when permission is granted and preview is available
    LaunchedEffect(hasCameraPermission, previewView, initialMode) {
        if (hasCameraPermission && previewView != null) {
            android.util.Log.d("CameraScreen", "LaunchedEffect triggered - initializing camera with mode: $initialMode")
            viewModel.initializeCameraWithMode(initialMode, context, previewView!!, lifecycleOwner)
        }
    }
    
    // Handle captured media - automatically use captured media without confirmation
    LaunchedEffect(uiState.isPhotoCaptured, uiState.isVideoCaptured) {
        if (uiState.isPhotoCaptured || uiState.isVideoCaptured) {
            capturedMediaUri = if (uiState.isPhotoCaptured) {
                uiState.lastCapturedPhoto?.toString()
            } else {
                uiState.lastCapturedVideo?.toString()
            }
            // Automatically navigate back with the captured media
            capturedMediaUri?.let { uri ->
                navController.previousBackStackEntry?.savedStateHandle?.set("capturedMediaUri", uri)
            }
            navController.navigateUp()
        }
    }
    
    // Video recording timer
    LaunchedEffect(uiState.isRecording) {
        if (uiState.isRecording) {
            recordingStartTime = System.currentTimeMillis()
            while (uiState.isRecording) {
                recordingDuration = System.currentTimeMillis() - recordingStartTime
                kotlinx.coroutines.delay(100) // Update every 100ms
            }
        } else {
            recordingDuration = 0L
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Camera Preview
            if (hasCameraPermission) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }.also {
                            previewView = it
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Camera Preview Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Camera permission is required to capture photos and videos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
            
            // Camera Controls - Always show if we have permission
            if (hasCameraPermission) {
                // Top controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash toggle
                    if (uiState.hasFlash) {
                        IconButton(
                            onClick = { viewModel.toggleFlash() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                if (uiState.isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Recording indicator with timer
                    if (uiState.isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatDuration(recordingDuration),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Switch camera button - hide during recording
                    if (!uiState.isRecording) {
                        IconButton(
                            onClick = { 
                                previewView?.let { view ->
                                    viewModel.switchCamera(context, view, lifecycleOwner)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Bottom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    // Capture controls based on mode
                    when (uiState.cameraMode) {
                        CameraMode.PHOTO -> {
                            // Photo capture button
                            IconButton(
                                onClick = {
                                    viewModel.takePhoto(context) { _ ->
                                        // Photo captured successfully
                                    }
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.White, CircleShape)
                                    .border(
                                        width = 4.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Camera,
                                    contentDescription = "Take Photo",
                                    tint = Color.Black,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        CameraMode.VIDEO -> {
                            // Video recording button
                            if (uiState.hasVideoCapture) {
                                Box {
                                    IconButton(
                                        onClick = {
                                            if (uiState.isRecording) {
                                                viewModel.stopVideoRecording()
                                            } else {
                                                // Check microphone permission before starting video recording
                                                if (hasMicrophonePermission) {
                                                    viewModel.startVideoRecording(context)
                                                } else {
                                                    microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(
                                                if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                CircleShape
                                            )
                                            .border(
                                                width = 4.dp,
                                                color = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                            contentDescription = if (uiState.isRecording) "Stop Recording" else "Start Recording",
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                    
                                    // Microphone permission indicator
                                    if (!hasMicrophonePermission && !uiState.isRecording) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .background(Color(0xFFFF9800), CircleShape)
                                                .border(2.dp, Color.White, CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Mic,
                                                contentDescription = "Microphone permission needed",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            
            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        // Add retry button for video recording errors
                        if (uiState.error!!.contains("Video") || uiState.error!!.contains("Camera")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.clearError()
                                            previewView?.let { view ->
                                                if (uiState.error!!.contains("session conflict")) {
                                                    viewModel.forceRestartCamera(context, view, lifecycleOwner)
                                                } else {
                                                    viewModel.reinitializeCamera(context, view, lifecycleOwner)
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Retry")
                                    }
                                    
                                    TextButton(
                                        onClick = {
                                            viewModel.clearError()
                                        }
                                    ) {
                                        Text("Dismiss")
                                    }
                                }
                                
                                // Add suggestion for photo capture if video fails
                                if (uiState.error!!.contains("Video")) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tip: Try taking a photo instead if video continues to fail",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Format duration in MM:SS format
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
