package com.koalatea.sedaily.network

enum class Status {
    RUNNING,
    SUCCESS,
    FAILED
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val message: String? = null) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        fun error(msg: String?) = NetworkState(Status.FAILED, msg)
    }
}

// FIXME :: Used a sealed class isntead
//sealed class NetworkState {
//    class Loading() : NetworkState()
//    class Loaded() : NetworkState()
//    data class Error(val message: String) : NetworkState()
//}