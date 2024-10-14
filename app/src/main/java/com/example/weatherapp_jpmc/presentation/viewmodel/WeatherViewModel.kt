package com.example.weatherapp_jpmc.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp_jpmc.domain.usecase.FetchWeatherUseCase
import com.example.weatherapp_jpmc.domain.model.Result
import com.example.weatherapp_jpmc.domain.model.WeatherResponse
import com.example.weatherapp_jpmc.domain.model.WeatherUiState
import com.example.weatherapp_jpmc.domain.model.WeatherUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val fetchWeatherUseCase: FetchWeatherUseCase,
    private val sharedPreferences: SharedPreferences,
    @ApplicationContext private val appContext: Context // Inject Application context

) : ViewModel() {

    private val _uiState = MutableLiveData<WeatherUiState>()
    val uiState: LiveData<WeatherUiState> = _uiState

    // Load the last searched city when the ViewModel is created
    init {
        val lastSearchedCity = retrieveLastSearchedCity()
        if (lastSearchedCity != null) {
            getWeather(lastSearchedCity)
        }
    }


    // Get weather data by latitude and longitude
    fun getWeatherByLocation(location: Location) {
        viewModelScope.launch {
            val result = fetchWeatherUseCase(appContext, location) // Fetch weather data by location

            when(result){
                is Result.Success -> {
                    getWeather(result.data) // Update getWeather with success data
                }
                is Result.Error -> {
                    _uiState.value = Error(result.message) // Update UI state with error message
                }
                Result.Loading -> {
                    // Loading state is already handled in setLoadingState
                }
            }
        }
    }

    // Function to get weather data by city name
    fun getWeather(city: String) {
        viewModelScope.launch {
            val result = fetchWeatherUseCase(city)

            handleWeatherResponse(result)

            storeLastSearchedCity(city)
        }
    }

    // Save the last searched city name in SharedPreferences
    private fun storeLastSearchedCity(city: String) {
        sharedPreferences.edit().putString("last_searched_city", city).apply()
    }

    // Retrieve the last searched city from SharedPreferences
    fun retrieveLastSearchedCity(): String? {
        return sharedPreferences.getString("last_searched_city", null)
    }

    // Handle the result and update UI state
    private fun handleWeatherResponse(result: Result<WeatherResponse>) {
        when (result) {
            is Result.Success -> {
                _uiState.value = Success(result.data)
            }
            is Result.Error -> {
                _uiState.value = Error(result.message)
            }
            Result.Loading -> {
                // Loading state is already handled in setLoadingState
            }
        }
    }
}