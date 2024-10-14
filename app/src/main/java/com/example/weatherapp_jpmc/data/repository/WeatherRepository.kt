package com.example.weatherapp_jpmc.data.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.weatherapp_jpmc.BuildConfig
import com.example.weatherapp_jpmc.data.remote.WeatherApiService
import com.example.weatherapp_jpmc.data.local.getCity
import com.example.weatherapp_jpmc.domain.model.Result
import com.example.weatherapp_jpmc.domain.model.WeatherResponse
import javax.inject.Inject

// Repository for managing weather data
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApiService,
    val owmApiKey: String = BuildConfig.API_KEY
) {
    // Fetch weather data and return a Result wrapper
    suspend fun fetchCityWeather(city: String): Result<WeatherResponse> {
        return try {
            val response = weatherApi.getWeatherByCity(city, owmApiKey, "imperial") // Make API call
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body) // Return successful result
                } else {
                    Result.Error("No data available") // Handle case where body is null
                }
            } else {
                Result.Error("Error: ${response.code()} - ${response.message()}") // Handle API error response
            }
        } catch (e: Exception) {
            Result.Error("Exception: ${e.message}") // Handle exceptions during the API call
        }
    }

    suspend fun fetchCityWeather(context: Context, location: Location): Result<String> {
        return try {
            // Create an instance of Geocoder
            val geocoder = Geocoder(context) // Ensure you have context

            // Directly call getCity()
            val city = geocoder.getCity(location.latitude, location.longitude)
            if (city != null) {
                Result.Success(city)
            } else {
                Result.Error("City not found")
            }
        } catch (e: Exception) {
            Result.Error("Exception: ${e.message}") // Handle exceptions during the API call
        }
    }
}