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
import androidx.core.content.FileProvider
import android.provider.MediaStore
import android.content.ContentValues
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LifecycleOwner
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

enum class CameraMode {
    PHOTO,
    VIDEO
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
    val error: String? = null,
    val cameraMode: CameraMode = CameraMode.PHOTO
)

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
    private var lifecycleOwner: LifecycleOwner? = null
    private var preview: Preview? = null
    
    fun initializeCamera(context: Context, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        viewModelScope.launch {
            try {
                Log.d("CameraViewModel", "=== CAMERA INITIALIZATION START ===")
                Log.d("CameraViewModel", "Mode: ${_uiState.value.cameraMode}")
                Log.d("CameraViewModel", "PreviewView: ${previewView != null}")
                Log.d("CameraViewModel", "LifecycleOwner: ${lifecycleOwner != null}")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                if (cameraProvider == null) {
                    cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    cameraExecutor = Executors.newSingleThreadExecutor()
                }
                
                // Set up the preview use case
                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Set up image capture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Set up video capture use case with more compatible quality settings
                try {
                    val recorder = Recorder.Builder()
                        .setQualitySelector(
                            QualitySelector.fromOrderedList(
                                listOf(
                                    Quality.LOWEST,  // Start with lowest for maximum compatibility
                                    Quality.SD,
                                    Quality.HIGHEST
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
                
                // Bind use cases to camera based on current mode
                try {
                    when (_uiState.value.cameraMode) {
                        CameraMode.PHOTO -> {
                            // Bind only preview and image capture for photo mode
                            camera = cameraProvider?.bindToLifecycle(
                                lifecycleOwner,
                                currentCameraSelector,
                                preview,
                                imageCapture
                            )
                            Log.d("CameraViewModel", "Camera bound for photo mode")
                        }
                        CameraMode.VIDEO -> {
                            // Bind preview and video capture for video mode
                            if (videoCapture != null) {
                                camera = cameraProvider?.bindToLifecycle(
                                    lifecycleOwner,
                                    currentCameraSelector,
                                    preview,
                                    videoCapture
                                )
                                Log.d("CameraViewModel", "Camera bound for video mode")
                            } else {
                                throw Exception("Video capture not available")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Failed to bind camera use cases: ${e.message}")
                    // Fallback to photo mode if video mode fails
                    try {
                        _uiState.value = _uiState.value.copy(cameraMode = CameraMode.PHOTO)
                        camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            currentCameraSelector,
                            preview,
                            imageCapture
                        )
                        videoCapture = null
                        Log.d("CameraViewModel", "Camera bound in photo mode after fallback")
                    } catch (fallbackException: Exception) {
                        Log.e("CameraViewModel", "Failed to bind camera even in photo mode: ${fallbackException.message}")
                        throw fallbackException
                    }
                }
                
                val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                val hasVideoCapture = videoCapture != null
                Log.d("CameraViewModel", "Camera initialized successfully!")
                Log.d("CameraViewModel", "Has flash: $hasFlash, Has video: $hasVideoCapture")
                Log.d("CameraViewModel", "Current mode: ${_uiState.value.cameraMode}")
                Log.d("CameraViewModel", "=== CAMERA INITIALIZATION END ===")
                
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
                    val savedUri = outputFileResults.savedUri ?: FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
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
        
        if (cameraProvider == null || videoCapture == null) {
            _uiState.value = _uiState.value.copy(error = "Camera not ready for video.")
            return
        }
        
        if (_uiState.value.cameraMode != CameraMode.VIDEO) {
            _uiState.value = _uiState.value.copy(error = "Camera not in video mode.")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)

        val localVideoCapture = videoCapture ?: return // Ensure videoCapture is not null

        // Since we're in dedicated video mode, no need to unbind/rebind
        try {
            // Proceed with recording setup
            try {
                val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
                    context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                    .setContentValues(
                        ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, "VID_${System.currentTimeMillis()}.mp4")
                            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        }
                    )
                    .build()

                recording = localVideoCapture.output
                    .prepareRecording(context, mediaStoreOutputOptions)
                    .withAudioEnabled() // Assumes microphone permission is granted
                    .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                        when (recordEvent) {
                            is VideoRecordEvent.Start -> {
                                _uiState.value = _uiState.value.copy(isRecording = true, isLoading = false, error = null)
                            }
                            is VideoRecordEvent.Finalize -> {
                                if (!recordEvent.hasError()) {
                                    val uri = recordEvent.outputResults.outputUri
                                    _uiState.value = _uiState.value.copy(isRecording = false, isLoading = false, lastCapturedVideo = uri, isVideoCaptured = true)
                                } else {
                                    recording?.close()
                                    recording = null
                                    _uiState.value = _uiState.value.copy(
                                        isRecording = false,
                                        isLoading = false,
                                        error = "Video recording failed: ${recordEvent.error} - ${recordEvent.cause?.message}"
                                    )
                                }
                            }
                            is VideoRecordEvent.Pause, is VideoRecordEvent.Resume, is VideoRecordEvent.Status -> {
                                // Handle other events if necessary
                            }
                        }
                    }
            } catch (securityException: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    isRecording = false, 
                    error = "Camera or microphone permission denied. Please grant permissions and try again."
                )
            } catch (exc: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRecording = false, error = "Failed to start video: ${exc.message}")
            }
        } catch (exc: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, isRecording = false, error = "Failed to start video: ${exc.message}")
        }
    }
    
    fun stopVideoRecording() {
        if (_uiState.value.isRecording) {
            recording?.stop() // This will trigger VideoRecordEvent.Finalize
            // isRecording and other states will be updated in the recordEvent callback
        }
        // Since we're in dedicated video mode, no need to rebind
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
    
    fun switchCamera(context: Context, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
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
        initializeCamera(context, previewView, lifecycleOwner)
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
    
    fun setCameraMode(mode: CameraMode, context: Context, previewView: PreviewView?) {
        Log.d("CameraViewModel", "Setting camera mode to: $mode")
        _uiState.value = _uiState.value.copy(cameraMode = mode)
        // Initialize camera if previewView is available
        if (previewView != null) {
            initializeCamera(context, previewView, lifecycleOwner ?: return)
        }
    }
    
    fun initializeCameraWithMode(mode: CameraMode, context: Context, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        Log.d("CameraViewModel", "Initializing camera with mode: $mode")
        _uiState.value = _uiState.value.copy(cameraMode = mode)
        initializeCamera(context, previewView, lifecycleOwner)
    }
    
    fun reinitializeCamera(context: Context, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        Log.d("CameraViewModel", "Reinitializing camera...")
        
        // Stop any ongoing recording first
        recording?.stop()
        recording = null
        
        // Clean up existing resources completely
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error unbinding camera: ${e.message}")
        }
        
        // Note: ProcessCameraProvider.shutdown() is a restricted API, so we skip it
        // The unbindAll() call above should be sufficient for cleanup
        
        camera = null
        videoCapture = null
        cameraProvider = null
        
        // Reset state
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            error = null
        )
        
        // Add a longer delay to ensure camera resources are fully released
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // 1 second delay
            initializeCamera(context, previewView, lifecycleOwner)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up all resources
        try {
            recording?.stop()
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error during cleanup: ${e.message}")
        }
    }
    
    fun forceRestartCamera(context: Context, previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        Log.d("CameraViewModel", "Force restarting camera...")
        
        // Complete cleanup
        try {
            recording?.stop()
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error during force restart cleanup: ${e.message}")
        }
        
        // Reset all variables
        camera = null
        videoCapture = null
        recording = null
        cameraProvider = null
        cameraExecutor = null
        
        // Reset state
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isCameraReady = false,
            hasVideoCapture = false,
            error = null
        )
        
        // Restart with longer delay and multiple attempts
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // 2 second delay for better resource cleanup
            
            // Try to initialize camera multiple times if needed
            var attempts = 0
            val maxAttempts = 3
            
            while (attempts < maxAttempts) {
                try {
                    initializeCamera(context, previewView, lifecycleOwner)
                    break // Success, exit loop
                } catch (e: Exception) {
                    attempts++
                    Log.e("CameraViewModel", "Camera initialization attempt $attempts failed: ${e.message}")
                    if (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(1000) // Wait 1 second before retry
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to restart camera after $maxAttempts attempts. Please try again."
                        )
                    }
                }
            }
        }
    }
}

