package com.example.reportviolation.ui.screens.maps

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ViewFlipper
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
    private lateinit var viewFlipper: ViewFlipper
    
    private val defaultLatLng = LatLng(11.0446, 76.9948) // Coimbatore fallback
    private var currentAddress: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize views
        locationText = findViewById(R.id.locationText)
        confirmBtn = findViewById(R.id.confirmBtn)
        
        // Set up confirm button
        confirmBtn.setOnClickListener {
            // Save address to shared prefs or pass to next screen
            currentAddress?.let { address ->
                Log.d("Location", "Confirmed Address: $address")
                // TODO: Save to SharedPreferences or pass back to calling activity
                finish()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable UI gestures
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Add fixed marker at center (not draggable)
        marker = mMap.addMarker(
            MarkerOptions()
                .position(defaultLatLng)
                .title("Selected Location")
                .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.custom_marker))
                .draggable(false) // Keep marker fixed
        )!!

        mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(defaultLatLng, 17f))

        // Listen for camera movement instead of marker drag
        mMap.setOnCameraIdleListener {
            val centerPosition = mMap.cameraPosition.target
            marker.position = centerPosition // Update marker to center
            getAddressFromLatLng(centerPosition)
        }

        // Initial address fetch
        getAddressFromLatLng(defaultLatLng)
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
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            addresses[0].getAddressLine(0) ?: "Unknown Location"
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

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
