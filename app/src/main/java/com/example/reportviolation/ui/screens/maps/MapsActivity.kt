package com.example.reportviolation.ui.screens.maps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reportviolation.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var locationText: TextView
    private lateinit var confirmBtn: Button
    
    private var initialLatitude: Double = 11.0446 // Coimbatore fallback
    private var initialLongitude: Double = 76.9948
    private var currentAddress: String? = null
    private var selectedLatLng: LatLng? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MapsActivity", "onCreate started")
        
        try {
            setContentView(R.layout.activity_maps)
            Log.d("MapsActivity", "setContentView completed")
            
            // Enable back button in action bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Select Location"
            
            // Get initial location from intent
            intent.extras?.let { bundle ->
                initialLatitude = bundle.getDouble("latitude", 11.0446)
                initialLongitude = bundle.getDouble("longitude", 76.9948)
                Log.d("MapsActivity", "Received location: $initialLatitude, $initialLongitude")
            }

            // Initialize views
            locationText = findViewById(R.id.locationText)
            if (locationText == null) {
                Log.e("MapsActivity", "locationText not found")
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            
            confirmBtn = findViewById(R.id.confirmBtn)
            if (confirmBtn == null) {
                Log.e("MapsActivity", "confirmBtn not found")
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            
            // Set up confirm button
            confirmBtn.setOnClickListener {
                selectedLatLng?.let { latLng ->
                    // Return the selected location to the calling activity
                    val resultIntent = Intent().apply {
                        putExtra("selected_latitude", latLng.latitude)
                        putExtra("selected_longitude", latLng.longitude)
                        putExtra("selected_address", currentAddress)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }

            // Initialize map
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as? SupportMapFragment
                ?: throw IllegalStateException("Map fragment not found")
            mapFragment.getMapAsync(this)
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error in onCreate: ${e.message}")
            // Return error result
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mMap = googleMap

            // Enable UI gestures
            mMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = false // Disable default toolbar
            }

            val initialLocation = LatLng(initialLatitude, initialLongitude)
            selectedLatLng = initialLocation

            // Add fixed marker at center (not draggable and NOT updating position)
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(initialLocation)
                    .title("Selected Location")
                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED))
                    .draggable(false) // Keep marker fixed at center
            ) ?: throw IllegalStateException("Failed to create marker")

            // Move camera to initial location
            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(initialLocation, 17f))

            // Listen for camera movement to update location (but NOT marker position)
            mMap.setOnCameraIdleListener {
                val centerPosition = mMap.cameraPosition.target
                selectedLatLng = centerPosition
                // DO NOT update marker position - keep it static!
                Log.d("MapsActivity", "Camera moved to: ${centerPosition.latitude}, ${centerPosition.longitude}")
                Log.d("MapsActivity", "Marker position: ${marker.position.latitude}, ${marker.position.longitude}")
                getAddressFromLatLng(centerPosition)
            }

            // Initial address fetch
            getAddressFromLatLng(initialLocation)
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error in onMapReady: ${e.message}")
            // Return error result
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Reverse geocode LatLng to address with loading state
    private fun getAddressFromLatLng(latLng: LatLng) {
        // Show loading state
        locationText.text = "Fetching address..."
        confirmBtn.isEnabled = false
        
        coroutineScope.launch {
            try {
                                 val address = withContext(Dispatchers.IO) {
                     val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                     try {
                         @Suppress("DEPRECATION")
                         val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                         if (!addresses.isNullOrEmpty()) {
                            val addressObj = addresses[0]
                            // Build a more detailed address
                            val addressParts = mutableListOf<String>()
                            
                            // Add sub-locality (neighborhood) if available
                            addressObj.subLocality?.let { if (it.isNotBlank()) addressParts.add(it) }
                            
                            // Add thoroughfare (street name) if available
                            addressObj.thoroughfare?.let { if (it.isNotBlank()) addressParts.add(it) }
                            
                            // Add locality (city district) if available
                            addressObj.locality?.let { if (it.isNotBlank() && !addressParts.contains(it)) addressParts.add(it) }
                            
                            // Add admin area (city) if available
                            addressObj.adminArea?.let { if (it.isNotBlank() && !addressParts.contains(it)) addressParts.add(it) }
                            
                            if (addressParts.isNotEmpty()) {
                                addressParts.joinToString(", ")
                            } else {
                                addressObj.getAddressLine(0) ?: "Unknown Location"
                            }
                        } else {
                            "Unknown Location"
                        }
                    } catch (e: IOException) {
                        Log.e("Location", "Geocoding error: ${e.message}")
                        "Error fetching address"
                    }
                }
                
                // Update UI on main thread
                locationText.text = address
                currentAddress = address
                confirmBtn.isEnabled = true
                
                Log.d("Location", "Selected Address: $address")
                
            } catch (e: Exception) {
                locationText.text = "Error fetching address"
                confirmBtn.isEnabled = false
                Log.e("Location", "Error: ${e.message}")
            }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button press
                setResult(RESULT_CANCELED)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
