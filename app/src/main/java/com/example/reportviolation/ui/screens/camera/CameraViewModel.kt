package com.example.reportviolation.ui.screens.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.core.CameraInfo
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Log

class CameraViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    fun initializeCamera(context: Context, previewView: PreviewView) {
        viewModelScope.launch {
            try {
                Log.d("CameraViewModel", "Initializing camera...")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                if (cameraProvider == null) {
                    cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    cameraExecutor = Executors.newSingleThreadExecutor()
                }
                
                // Set up the preview use case
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Set up image capture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Set up video capture use case with fallback quality
                try {
                    val recorder = Recorder.Builder()
                        .setQualitySelector(
                            QualitySelector.fromOrderedList(
                                listOf(
                                    Quality.HIGHEST,
                                    Quality.SD,
                                    Quality.LOWEST
                                )
                            )
                        )
                        .build()
                    videoCapture = VideoCapture.withOutput(recorder)
                    Log.d("CameraViewModel", "Video capture initialized successfully")
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Failed to initialize video capture: ${e.message}")
                    videoCapture = null
                }
                
                // Check if video capture is supported
                if (!cameraProvider?.hasCamera(currentCameraSelector)!!) {
                    throw Exception("Camera not available")
                }
                
                // Unbind any bound use cases before rebinding
                cameraProvider?.unbindAll()
                
                // Bind use cases to camera
                camera = cameraProvider?.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    currentCameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
                
                val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                val hasVideoCapture = videoCapture != null
                Log.d("CameraViewModel", "Camera initialized. Has flash: $hasFlash, Has video: $hasVideoCapture")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCameraReady = true,
                    hasFlash = hasFlash,
                    hasVideoCapture = hasVideoCapture,
                    isFlashOn = false // Reset flash state when switching cameras
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun takePhoto(context: Context, onPhotoCaptured: (Uri) -> Unit) {
        val imageCapture = imageCapture ?: return
        
        // Create timestamped file
        val photoFile = File(
            context.getExternalFilesDir(null),
            "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    _uiState.value = _uiState.value.copy(
                        lastCapturedPhoto = savedUri,
                        isPhotoCaptured = true
                    )
                    onPhotoCaptured(savedUri)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
            }
        )
    }
    
    fun startVideoRecording(context: Context) {
        Log.d("CameraViewModel", "Starting video recording...")
        val videoCapture = videoCapture ?: run {
            Log.e("CameraViewModel", "Video capture not initialized")
            _uiState.value = _uiState.value.copy(
                error = "Video capture not initialized",
                isRecording = false
            )
            return
        }
        
        // Check if video recording is supported
        if (videoCapture == null) {
            Log.e("CameraViewModel", "Video capture not supported on this camera")
            _uiState.value = _uiState.value.copy(
                error = "Video recording is not supported on this device",
                isRecording = false
            )
            return
        }
        
        try {
            val videoFile = File(
                context.getExternalFilesDir(null),
                "video_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"
            )
            
            val outputOptions = FileOutputOptions.Builder(videoFile).build()
            
            recording = videoCapture.output
                .prepareRecording(context, outputOptions)
                .apply { 
                    // Check for audio permission before enabling audio
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            _uiState.value = _uiState.value.copy(isRecording = true)
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (recordEvent.hasError()) {
                                Log.e("CameraViewModel", "Video recording failed: ${recordEvent.error}")
                                _uiState.value = _uiState.value.copy(
                                    error = "Video capture failed: ${recordEvent.error}",
                                    isRecording = false
                                )
                            } else {
                                Log.d("CameraViewModel", "Video recording completed successfully")
                                val savedUri = Uri.fromFile(videoFile)
                                _uiState.value = _uiState.value.copy(
                                    lastCapturedVideo = savedUri,
                                    isVideoCaptured = true,
                                    isRecording = false
                                )
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to start video recording: ${e.message}",
                isRecording = false
            )
        }
    }
    
    fun stopVideoRecording() {
        recording?.stop()
        recording = null
        _uiState.value = _uiState.value.copy(isRecording = false)
    }
    
    fun toggleFlash() {
        camera?.let { camera ->
            if (camera.cameraInfo.hasFlashUnit()) {
                val newFlashState = !_uiState.value.isFlashOn
                Log.d("CameraViewModel", "Toggling flash to: $newFlashState")
                camera.cameraControl.enableTorch(newFlashState)
                _uiState.value = _uiState.value.copy(isFlashOn = newFlashState)
            } else {
                Log.d("CameraViewModel", "Flash not available on this camera")
            }
        }
    }
    
    fun switchCamera(context: Context, previewView: PreviewView) {
        // Toggle between front and back camera
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        _uiState.value = _uiState.value.copy(
            isFrontCamera = currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        )
        
        // Reinitialize camera with new selector
        initializeCamera(context, previewView)
    }
    
    fun resetCaptureState() {
        _uiState.value = _uiState.value.copy(
            isPhotoCaptured = false,
            isVideoCaptured = false,
            lastCapturedPhoto = null,
            lastCapturedVideo = null,
            error = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraExecutor?.shutdown()
    }
}

data class CameraUiState(
    val isLoading: Boolean = false,
    val isCameraReady: Boolean = false,
    val isRecording: Boolean = false,
    val isPhotoCaptured: Boolean = false,
    val isVideoCaptured: Boolean = false,
    val lastCapturedPhoto: Uri? = null,
    val lastCapturedVideo: Uri? = null,
    val hasFlash: Boolean = false,
    val hasVideoCapture: Boolean = false,
    val isFlashOn: Boolean = false,
    val isFrontCamera: Boolean = false,
    val error: String? = null
)
