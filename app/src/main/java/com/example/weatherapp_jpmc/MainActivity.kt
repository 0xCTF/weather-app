package com.example.weatherapp_jpmc

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.example.weatherapp_jpmc.data.local.LocationService
import com.example.weatherapp_jpmc.presentation.screen.WeatherScreen
import com.example.weatherapp_jpmc.presentation.theme.Weatherapp_jpmcTheme
import com.example.weatherapp_jpmc.presentation.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationService: LocationService

    // Inject WeatherViewModel
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Weatherapp_jpmcTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        // Load the last searched city
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else {
            if (isGooglePlayServicesAvailable()) {
                getLocation()
            } else {
                Toast.makeText(this, "Google Play Services are not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocation() {
        lifecycleScope.launch {
            try {
                val location = locationService.getLastKnownLocation()
                val lastSearchedCity = viewModel.retrieveLastSearchedCity()

                if(lastSearchedCity == null) {
                    if (location != null) {
                        // Pass the city to the ViewModel
                        viewModel.getWeatherByLocation(location)

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "City is not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun requestLocationPermission() {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (isGooglePlayServicesAvailable()) {
                getLocation()
            } else {
                Toast.makeText(this, "Google Play Services are not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }
}