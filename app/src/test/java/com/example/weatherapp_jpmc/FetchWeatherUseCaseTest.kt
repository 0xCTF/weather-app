package com.example.weatherapp_jpmc

import com.example.weatherapp_jpmc.data.repository.WeatherRepository
import com.example.weatherapp_jpmc.domain.model.Main
import com.example.weatherapp_jpmc.domain.usecase.FetchWeatherUseCase
import junit.framework.TestCase.assertEquals
import com.example.weatherapp_jpmc.domain.model.Result
import com.example.weatherapp_jpmc.domain.model.Sys
import com.example.weatherapp_jpmc.domain.model.Weather
import com.example.weatherapp_jpmc.domain.model.WeatherResponse
import com.example.weatherapp_jpmc.domain.model.Wind
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After


@ExperimentalCoroutinesApi
class FetchWeatherUseCaseTest {

    private lateinit var fetchWeatherUseCase: FetchWeatherUseCase
    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mock()
        fetchWeatherUseCase = FetchWeatherUseCase(repository)
    }


    @Test
    fun `fetchWeather returns data from repository`()  {
        runTest {
            // Given
            val weatherResponse = WeatherResponse(
                weather = listOf(Weather("Clear", "X")),
                main = Main(
                    temp = 25.0,
                    pressure = 1010.0,
                    humidity = 60.0
                ),
                wind = Wind(speed = 10.0),
                name = "New York",
                sys = Sys(country = "US")
            )

            `when`(repository.fetchCityWeather("New York")).thenReturn(Result.Success(weatherResponse))

            // When
            val result = fetchWeatherUseCase("New York")

            // Then
            assertEquals(weatherResponse, (result as Result.Success).data)
        }
    }

    @Test
    fun `fetchWeather returns error for unknown city`() {
        runTest {
            // Given
            `when`(repository.fetchCityWeather("Unknown City")).thenReturn(Result.Error("No data available"))

            // When
            val result = fetchWeatherUseCase("Unknown City")

            // Then
            assertTrue(result is Result.Error)
            assertEquals("No data available", (result as Result.Error).message)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main dispatcher to the original
    }

}
