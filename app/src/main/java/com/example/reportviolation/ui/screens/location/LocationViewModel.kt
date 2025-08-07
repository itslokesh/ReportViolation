package com.example.reportviolation.ui.screens.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    
    @SuppressLint("MissingPermission")
    fun initializeLocationServices(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                // Check if location is enabled
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val isLocationEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                
                if (!isLocationEnabled) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location services are disabled. Please enable GPS."
                    )
                    return@launch
                }
                
                // Get last known location
                val lastLocation = fusedLocationClient?.lastLocation?.await()
                if (lastLocation != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude),
                        isLocationReady = true
                    )
                }
                
                // Set up location updates
                setupLocationUpdates()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateDistanceMeters(10f)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _uiState.value = _uiState.value.copy(
                        currentLocation = LatLng(location.latitude, location.longitude),
                        isLocationReady = true,
                        accuracy = location.accuracy
                    )
                }
            }
        }
        
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null
        )
    }
    
    fun getCurrentLocation(): LatLng? {
        return _uiState.value.currentLocation
    }
    
    fun getLocationAccuracy(): Float? {
        return _uiState.value.accuracy
    }
    
    fun isLocationAccurate(): Boolean {
        val accuracy = _uiState.value.accuracy ?: return false
        return accuracy <= 20f // Consider accurate if within 20 meters
    }
    
    fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude, latLng1.longitude,
            latLng2.latitude, latLng2.longitude,
            results
        )
        return results[0]
    }
    
    @SuppressLint("MissingPermission")
    fun getAddressFromLocation(context: Context, latLng: LatLng, onAddressReceived: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val geocoder = android.location.Geocoder(context)
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressString = buildString {
                        if (address.thoroughfare != null) append(address.thoroughfare)
                        if (address.subThoroughfare != null) append(", ${address.subThoroughfare}")
                        if (address.locality != null) append(", ${address.locality}")
                        if (address.adminArea != null) append(", ${address.adminArea}")
                        if (address.postalCode != null) append(" ${address.postalCode}")
                    }
                    onAddressReceived(addressString)
                } else {
                    onAddressReceived("Unknown location")
                }
            } catch (e: Exception) {
                onAddressReceived("Unable to get address")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }
    }
}

data class LocationUiState(
    val isLoading: Boolean = false,
    val isLocationReady: Boolean = false,
    val currentLocation: LatLng? = null,
    val accuracy: Float? = null,
    val error: String? = null
)
