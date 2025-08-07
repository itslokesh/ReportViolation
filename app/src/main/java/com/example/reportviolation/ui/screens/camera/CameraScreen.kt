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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    viewModel: CameraViewModel = viewModel()
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasMicrophonePermission by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var capturedMediaUri by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    val context = LocalContext.current
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
        if (isGranted && previewView != null) {
            viewModel.initializeCamera(context, previewView!!)
        }
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
    
    // Handle captured media
    LaunchedEffect(uiState.isPhotoCaptured, uiState.isVideoCaptured) {
        if (uiState.isPhotoCaptured || uiState.isVideoCaptured) {
            capturedMediaUri = if (uiState.isPhotoCaptured) {
                uiState.lastCapturedPhoto?.toString()
            } else {
                uiState.lastCapturedVideo?.toString()
            }
            showPreview = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Evidence") },
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
                    modifier = Modifier.fillMaxSize(),
                    update = { newPreviewView ->
                        previewView = newPreviewView
                        if (hasCameraPermission) {
                            viewModel.initializeCamera(context, newPreviewView)
                        }
                    }
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
                    
                    // Recording indicator
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
                        }
                    }
                    
                    // Switch camera button
                    IconButton(
                        onClick = { 
                            previewView?.let { view ->
                                viewModel.switchCamera(context, view)
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
                
                // Bottom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    // Capture controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Capture button (Photo)
                        IconButton(
                            onClick = {
                                if (!uiState.isRecording) {
                                    viewModel.takePhoto(context) { _ ->
                                        // Photo captured successfully
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (uiState.isRecording) Color.Gray else Color.White,
                                    CircleShape
                                )
                                .border(
                                    width = 4.dp,
                                    color = if (uiState.isRecording) Color.Gray else Color.White,
                                    shape = CircleShape
                                ),
                            enabled = !uiState.isRecording
                        ) {
                            Icon(
                                Icons.Default.Camera,
                                contentDescription = "Take Photo",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Video recording button (only show if supported)
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
                    Row(
                        modifier = Modifier.padding(16.dp),
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
                }
            }
        }
        
        // Preview dialog
        if (showPreview && capturedMediaUri != null) {
            AlertDialog(
                onDismissRequest = { 
                    showPreview = false
                    viewModel.resetCaptureState()
                },
                title = {
                    Text("Media Captured")
                },
                text = {
                    Text("Your ${if (uiState.isVideoCaptured) "video" else "photo"} has been captured successfully.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPreview = false
                            // Navigate back to Report Violation screen with the captured media
                            capturedMediaUri?.let { uri ->
                                navController.previousBackStackEntry?.savedStateHandle?.set("capturedMediaUri", uri)
                            }
                            navController.navigateUp()
                        }
                    ) {
                        Text("Use This Media")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPreview = false
                            capturedMediaUri = null
                            viewModel.resetCaptureState()
                        }
                    ) {
                        Text("Retake")
                    }
                }
            )
        }
    }
}
