package com.koalatea.sedaily.repository

import androidx.annotation.MainThread
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.model.Download
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.downloader.DownloadManager
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodeDetailsRepository constructor(
        private val api: SEDailyApi,
        private val db: AppDatabase,
        private val downloadManager: DownloadManager
) {

    suspend fun fetchEpisodeDetails(episodeId: String) = withContext(Dispatchers.IO) {
        val response = api.getEpisodeAsync(episodeId).await()
        val episode = response.body()
        if (response.isSuccessful && episode != null) {
            val cachedEpisode = db.episodeDao().findById(episodeId)

            // In case that was requested before upvote or bookmark calls are done.
            Resource.Success(episode.copy(upvoted = cachedEpisode.upvoted, bookmarked = cachedEpisode.bookmarked).apply {
                downloadedId = db.downloadDao().findById(episodeId)?.downloadId

                // Get local uriString if file was already download downloaded otherwise use remote url.
                downloadedId?.let {
                    val downloadStatus = getDownloadStatus(it)
                    uri = if (downloadStatus is DownloadStatus.Downloaded) {
                        downloadStatus.uriString
                    } else {
                        episode.httpsMp3Url
                    }
                } ?: run {
                    uri = episode.httpsMp3Url
                }

                // Continue from where the user left off.
                db.listenedDao().findById(episodeId)?.let {
                    startPosition = it.startPosition
                    total = it.total
                }
            })
        } else {
            Resource.Error(response.errorBody().toException())
        }
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

    @MainThread
    fun downloadEpisode(episode: Episode) = episode.httpsMp3Url?.let { url ->
        downloadManager.downloadEpisode(episode._id, url, episode.titleString ?: episode._id)
    }

    @MainThread
    fun getDownloadStatus(downloadId: Long) = downloadManager.getDownloadStatus(downloadId)

}