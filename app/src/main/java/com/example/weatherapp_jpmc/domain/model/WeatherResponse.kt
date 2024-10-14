package com.example.weatherapp_jpmc.domain.model

// Data classes for parsing weather data
data class WeatherResponse(
    val weather: List<Weather>, // List of weather conditions
    val main: Main, // Main weather data
    val wind: Wind, // Wind weather data
    val name: String, // City name
    val sys: Sys // Sys contains country
)

data class Weather(val description: String, val icon: String) // Weather description and icon
data class Main(val temp: Double, val pressure: Double, val humidity: Double) // Main data containing temperature
data class Wind(val speed: Double) // Wind data containing temperature
data class Sys(val country: String)