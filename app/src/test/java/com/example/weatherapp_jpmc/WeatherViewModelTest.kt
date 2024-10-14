package com.example.weatherapp_jpmc

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.weatherapp_jpmc.domain.model.*
import com.example.weatherapp_jpmc.domain.usecase.FetchWeatherUseCase
import com.example.weatherapp_jpmc.presentation.viewmodel.WeatherViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class WeatherViewModelTest {

    // Rule for LiveData testing
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Coroutine test dispatcher
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var fetchWeatherUseCase: FetchWeatherUseCase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var observer: Observer<WeatherUiState>
    private lateinit var appContext: Context

    @Before
    fun setup() {
        // Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Mock dependencies
        fetchWeatherUseCase = mock(FetchWeatherUseCase::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        appContext = mock(Context::class.java)

        // Mock SharedPreferences methods
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        doNothing().`when`(editor).apply()

        // Initialize ViewModel with mocked dependencies
        weatherViewModel = WeatherViewModel(
            fetchWeatherUseCase = fetchWeatherUseCase,
            sharedPreferences = sharedPreferences,
            appContext = appContext
        )

        // Mock observer for LiveData
        observer = mock(Observer::class.java) as Observer<WeatherUiState>
    }

    @Test
    fun `getWeather should update UI state when use case returns data`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            weather = listOf(Weather("Clear", "X")),
            main = Main(
                temp = 25.0,
                pressure = 1010.0,
                humidity = 60.0
            ),
            wind = Wind(speed = 10.0),
            name = "tampa",
            sys = Sys(country = "US")
        )

        // Mock the use case to return the expected success result
        `when`(fetchWeatherUseCase("tampa")).thenReturn(Result.Success(weatherResponse))

        // Attach observer to LiveData
        weatherViewModel.uiState.observeForever(observer)

        // When
        weatherViewModel.getWeather("tampa")

        // Then
        verify(observer).onChanged(WeatherUiState.Success(weatherResponse)) // Verify success state
        verifyNoMoreInteractions(observer) // Ensure no further interactions with observer
    }


    @Test
    fun `getWeather should handle error response from use case`() {
        runTest {
            // Given
            `when`(fetchWeatherUseCase("Unknown City")).thenReturn(Result.Error("City is not found"))

            // Attach observer to LiveData
            weatherViewModel.uiState.observeForever(observer)

            // When
            weatherViewModel.getWeather("Unknown City")

            // Then
            verify(observer).onChanged(WeatherUiState.Error("City is not found")) // Verify error state
            verifyNoMoreInteractions(observer)
        }
    }

    @Test
    fun `getWeatherByLocation should update UI state when use case returns data`() = runTest {
        // Given
        val location = mock(Location::class.java)
        val cityName = "San Francisco"
        val weatherResponse = WeatherResponse(
            weather = listOf(Weather(description = "Cloudy", icon = "02d")),
            main = Main(temp = 18.0, pressure = 1015.0, humidity = 70.0),
            wind = Wind(speed = 5.0),
            name = cityName,
            sys = Sys(country = "US")
        )

        // Mock the use case to return the city name when called with location
        `when`(fetchWeatherUseCase(appContext, location)).thenReturn(Result.Success(cityName))
        // Mock the use case to return weather data when called with city name
        `when`(fetchWeatherUseCase(cityName)).thenReturn(Result.Success(weatherResponse))

        // Attach observer to LiveData
        weatherViewModel.uiState.observeForever(observer)

        // When
        weatherViewModel.getWeatherByLocation(location)

        // Advance coroutines
        advanceUntilIdle()

        // Then
        val inOrder = inOrder(observer)
        inOrder.verify(observer).onChanged(WeatherUiState.Success(weatherResponse)) // Verify success state

        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `getWeatherByLocation should handle error response from use case`() = runTest {
        // Given
        val location = mock(Location::class.java)
        val errorMessage = "City is not found"
        `when`(fetchWeatherUseCase(appContext, location)).thenReturn(Result.Error(errorMessage))

        // Attach observer to LiveData
        weatherViewModel.uiState.observeForever(observer)

        // When
        weatherViewModel.getWeatherByLocation(location)

        // Advance coroutines
        advanceUntilIdle()

        // Then
        val inOrder = inOrder(observer)
        inOrder.verify(observer).onChanged(WeatherUiState.Error(errorMessage)) // Verify error state

        verifyNoMoreInteractions(observer)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main dispatcher to the original
    }
}
