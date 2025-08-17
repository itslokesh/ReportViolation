package com.example.reportviolation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController
import android.media.MediaPlayer


@Composable
fun MediaPreviewDialog(
    mediaUri: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = mediaUri.endsWith(".mp4") || mediaUri.endsWith(".mov") || mediaUri.contains("video")
    
    // Video player launcher
    val videoPlayerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Dimmed background scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Media content container
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = if (isVideo) 0.3f else 0.1f))
            ) {
                // Loading indicator state for video
                var isVideoLoading by remember { mutableStateOf(isVideo) }
                var isBuffering by remember { mutableStateOf(false) }

                when {
                    isVideo -> {
                        // Inline playback using VideoView so the URL is fetched directly
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val controller = MediaController(ctx)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setVideoURI(Uri.parse(mediaUri))
                                    setOnPreparedListener { mp ->
                                        isVideoLoading = false
                                        mp.isLooping = false
                                        // Listen for buffering events
                                        mp.setOnInfoListener { _: MediaPlayer?, what: Int, _: Int ->
                                            when (what) {
                                                MediaPlayer.MEDIA_INFO_BUFFERING_START -> { isBuffering = true; true }
                                                MediaPlayer.MEDIA_INFO_BUFFERING_END -> { isBuffering = false; true }
                                                else -> false
                                            }
                                        }
                                        start()
                                    }
                                    setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
                                        isVideoLoading = false
                                        isBuffering = false
                                        false
                                    }
                                }
                            }
                        )
                        if (isVideoLoading || isBuffering) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                    else -> {
                        // Photo display without oversized black borders
                        Image(
                            painter = rememberAsyncImagePainter(model = mediaUri),
                            contentDescription = "Photo preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Media type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isVideo) "VIDEO" else "PHOTO",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
