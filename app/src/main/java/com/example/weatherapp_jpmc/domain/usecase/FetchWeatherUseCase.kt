package com.example.weatherapp_jpmc.domain.usecase

import android.content.Context
import android.location.Location
import com.example.weatherapp_jpmc.domain.model.Result
import com.example.weatherapp_jpmc.data.repository.WeatherRepository
import com.example.weatherapp_jpmc.domain.model.WeatherResponse
import javax.inject.Inject

class FetchWeatherUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(city: String): Result<WeatherResponse> {
        return repository.fetchCityWeather(city)
    }

    suspend operator fun invoke(context: Context, location: Location): Result<String> {
        return repository.fetchCityWeather(context,location)
    }
}