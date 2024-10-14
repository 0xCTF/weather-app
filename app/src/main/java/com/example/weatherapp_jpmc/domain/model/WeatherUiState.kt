package com.example.weatherapp_jpmc.domain.model

// Sealed class for UI state representation
sealed class WeatherUiState {
    data class Success(val data: WeatherResponse) : WeatherUiState() // Success state with data
    data class Error(val message: String) : WeatherUiState() // Error state with message
}