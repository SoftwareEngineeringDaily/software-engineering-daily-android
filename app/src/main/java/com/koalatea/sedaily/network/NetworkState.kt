package com.koalatea.sedaily.network

sealed class NetworkState {
    object Loading : NetworkState()
    data class Loaded(val itemsCount: Int) : NetworkState()
    data class Error(val message: String) : NetworkState()
}