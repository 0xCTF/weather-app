package com.example.weatherapp_jpmc.domain.model

// Sealed class to represent the result of a data operation
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>() // Success case
    data class Error(val message: String) : Result<Nothing>() // Error case
    object Loading : Result<Nothing>() // Loading state
}