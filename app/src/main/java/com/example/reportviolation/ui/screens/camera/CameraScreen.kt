package com.example.reportviolation.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.reportviolation.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var capturedMediaUri by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Check initial camera permission
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Request camera permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
            // Camera Preview Area (Placeholder for now)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Camera preview placeholder
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
                        text = if (isRecording) "Recording..." else "Camera Preview",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    if (!hasCameraPermission) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Camera permission is required",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

            // Camera Controls
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
                    IconButton(
                        onClick = { /* TODO: Toggle flash */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }

                    // Recording indicator
                    if (isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Settings
                    IconButton(
                        onClick = { /* TODO: Camera settings */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
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
                    // Mode selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Photo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Video",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Capture controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery button
                        IconButton(
                            onClick = { /* TODO: Open gallery */ },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Capture button
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    // Stop recording
                                    isRecording = false
                                    capturedMediaUri = "sample_video_uri" // Placeholder
                                    showPreview = true
                                } else {
                                    // Take photo
                                    capturedMediaUri = "sample_photo_uri" // Placeholder
                                    showPreview = true
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (isRecording) Color.Red else Color.White,
                                    CircleShape
                                )
                                .border(
                                    width = 4.dp,
                                    color = if (isRecording) Color.Red else Color.White,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Camera,
                                contentDescription = if (isRecording) "Stop Recording" else "Take Photo",
                                tint = if (isRecording) Color.White else Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Switch camera button
                        IconButton(
                            onClick = { /* TODO: Switch camera */ },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Preview dialog
        if (showPreview && capturedMediaUri != null) {
            AlertDialog(
                onDismissRequest = { showPreview = false },
                title = {
                    Text("Media Captured")
                },
                text = {
                    Text("Your ${if (isRecording) "video" else "photo"} has been captured successfully.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPreview = false
                            // Navigate back to Report Violation screen with the captured media
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
                        }
                    ) {
                        Text("Retake")
                    }
                }
            )
        }
    }
}
