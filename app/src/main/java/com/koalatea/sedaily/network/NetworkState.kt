package com.koalatea.sedaily.network

sealed class NetworkState {
    object Loading : NetworkState()
    object Loaded : NetworkState()
    data class Error(val message: String) : NetworkState()
}