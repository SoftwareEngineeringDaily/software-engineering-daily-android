package com.koalatea.sedaily.feature.downloader

sealed class DownloadStatus {
    object Initial : DownloadStatus()
    object Unknown : DownloadStatus()
    data class Downloading(val progress: Float) : DownloadStatus()
    data class Downloaded(val uriString: String) : DownloadStatus()
    data class Error(val reason: String? = null) : DownloadStatus()
}