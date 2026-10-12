package com.saboon.project_2511sch.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
    class Idle<T> : Resource<T>()
}

/**
 * Converts the data of a [Resource.Success] with [transform] and keeps every other state as it is.
 * Lets a layer map a result to its own model without unwrapping and rewrapping each state by hand.
 */
@Suppress("UNCHECKED_CAST")
fun <T, R> Resource<T>.mapData(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data as T))
    is Resource.Error -> Resource.Error(message ?: "An unknown error occurred")
    is Resource.Loading -> Resource.Loading()
    is Resource.Idle -> Resource.Idle()
}
