package com.example.weatherapp_jpmc.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.location.Geocoder
import android.location.Address
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationService @Inject constructor(
    private val context: Context, // Application context
    private val fusedLocationProviderClient: FusedLocationProviderClient // Location provider client
) {

    // Check if location permission is granted
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Retrieve the last known location if permission is granted, handle failure gracefully
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (isLocationPermissionGranted()) {
            try {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location) // Resume with the location
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception) // Resume with exception on failure
                    }
            } catch (_: SecurityException) {
                // Resume with null if a SecurityException occurs
                continuation.resume(null)
            }
        } else {
            // Resume with null if permission is not granted
            continuation.resume(null)
        }
    }
}

// Extension function to retrieve city name from latitude and longitude using Geocoder
suspend fun Geocoder.getCity(
    latitude: Double,
    longitude: Double,
): String? = withContext(Dispatchers.IO) {
    try {
        val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For newer Android versions, use the callback-based API
            suspendCancellableCoroutine { continuation ->
                getFromLocation(latitude, longitude, 1) {
                    continuation.resume(it)
                }
            }
        } else {
            // For older Android versions, use the deprecated blocking call
            @Suppress("DEPRECATION")
            suspendCancellableCoroutine { continuation ->
                val addressList = getFromLocation(latitude, longitude, 1)
                continuation.resume(addressList)
            }
        }

        // Return the city from the first address, if available
        addresses?.firstOrNull()?.locality
    } catch (e: Exception) {
        Log.d("LocationService", "Error getting city", e)
        null // Return null on failure
    }
}