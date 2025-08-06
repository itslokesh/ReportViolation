package com.example.reportviolation.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

/**
 * Utility class for handling runtime permissions in Jetpack Compose
 */
object PermissionUtil {
    
    /**
     * Check if a permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if multiple permissions are granted
     */
    fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get all required permissions for the app
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    /**
     * Get camera permissions
     */
    fun getCameraPermissions(): Array<String> {
        return arrayOf(Manifest.permission.CAMERA)
    }
    
    /**
     * Get location permissions
     */
    fun getLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    /**
     * Get storage permissions
     */
    fun getStoragePermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

/**
 * Composable function to request a single permission
 */
@Composable
fun rememberPermissionLauncher(
    permission: String,
    onPermissionResult: (Boolean) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    onPermissionResult(isGranted)
}

/**
 * Composable function to request multiple permissions
 */
@Composable
fun rememberMultiplePermissionsLauncher(
    permissions: Array<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissionsMap ->
    onPermissionsResult(permissionsMap)
}

/**
 * State holder for permission status
 */
@Composable
fun rememberPermissionState(
    context: Context,
    permission: String
): State<Boolean> {
    return remember {
        mutableStateOf(PermissionUtil.isPermissionGranted(context, permission))
    }
}

/**
 * State holder for multiple permissions status
 */
@Composable
fun rememberMultiplePermissionsState(
    context: Context,
    permissions: Array<String>
): State<Boolean> {
    return remember {
        mutableStateOf(PermissionUtil.arePermissionsGranted(context, permissions))
    }
} 