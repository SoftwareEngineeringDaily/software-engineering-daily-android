package com.koalatea.sedaily.feature.episodedetail

import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.table.Download
import com.koalatea.sedaily.database.table.Episode
import com.koalatea.sedaily.feature.downloader.DownloadManager
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodeDetailsRepository constructor(
        private val api: SEDailyApi,
        private val db: AppDatabase,
        private val downloadManager: DownloadManager
) {

    suspend fun fetchEpisodeDetails(episodeId: String) = withContext(Dispatchers.IO) {
        val episode = db.episodeDao().findById(episodeId)
        val download = db.downloadDao().findById(episodeId)
        download?.let {
            episode.isDownloaded = downloadManager.isDownloaded(download.downloadId)
        }

        Resource.Success(episode)
    }

    suspend fun addDownload(episodeId: String, downloadId: Long) {
        withContext(Dispatchers.IO) {
            db.downloadDao().insert(Download(episodeId, downloadId))
        }
    }

    suspend fun deleteDownload(episode: Episode) {
        withContext(Dispatchers.IO) {
            val download = db.downloadDao().findById(episode._id)

            download?.let {
                // Delete DB entry.
                db.downloadDao().delete(download)

                // Delete local file
                downloadManager.deleteDownload(episode._id)
            }
        }
    }

    fun downloadEpisode(episode: Episode) = episode.mp3?.let { url ->
        downloadManager.downloadEpisode(episode._id, url, episode.titleString ?: episode._id)
    }

    fun getDownloadStatus(downloadId: Long) = downloadManager.getDownloadStatus(downloadId)

}