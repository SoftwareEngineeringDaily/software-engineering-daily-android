package com.koalatea.sedaily.feature.downloader

data class DownloadEpisodeEvent(
        val progress: Int? = null,
        val episodeId: String? = null,
        val url: String? = null
)