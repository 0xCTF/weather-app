package com.example.weatherapp_jpmc.di

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherapp_jpmc.data.remote.WeatherApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.weatherapp_jpmc.data.local.LocationService
import com.example.weatherapp_jpmc.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton




@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/") // Base URL for the API
            .addConverterFactory(GsonConverterFactory.create()) // Converter for JSON to Kotlin objects
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java) // Create the API service instance
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(weatherApi: WeatherApiService): WeatherRepository {
        return WeatherRepository(weatherApi)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun provideLocationService(
        fusedLocationProviderClient: FusedLocationProviderClient,
        @ApplicationContext context: Context
    ): LocationService {
        return LocationService( context, fusedLocationProviderClient)
    }
}