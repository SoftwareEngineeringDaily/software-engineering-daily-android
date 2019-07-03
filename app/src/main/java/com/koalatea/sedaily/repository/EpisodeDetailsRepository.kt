package com.koalatea.sedaily.repository

import androidx.annotation.MainThread
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.model.Download
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.feature.downloader.DownloadManager
import com.koalatea.sedaily.feature.downloader.DownloadStatus
import com.koalatea.sedaily.network.NetworkManager
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.network.SEDailyApi
import com.koalatea.sedaily.network.toException
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class EpisodeDetailsRepository constructor(
        private val api: SEDailyApi,
        private val db: AppDatabase,
        private val downloadManager: DownloadManager,
        private val networkManager: NetworkManager
) {

    suspend fun fetchEpisodeDetails(episodeId: String, cachedEpisode: Episode? = null) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.getEpisodeAsync(episodeId).await() }
        val episode = response?.body()
        if (response?.isSuccessful == true && episode != null) {
            @Suppress("NAME_SHADOWING")
            val cachedEpisode = cachedEpisode ?: db.episodeDao().findById(episodeId)?.episode

            // In case that was requested before upvote or bookmark calls are done.
            Resource.Success(EpisodeDetails(
                    episode = episode
                            .copy(
                                    upvoted = cachedEpisode?.upvoted ?: episode.upvoted,
                                    score = cachedEpisode?.score,
                                    bookmarked = cachedEpisode?.bookmarked ?: episode.bookmarked)
                            .apply {
                                downloadedId = db.downloadDao().findById(episodeId)?.downloadId

                                // Get local uriString if file was already download downloaded otherwise use remote url.
                                downloadedId?.let {
                                    val downloadStatus = getDownloadStatus(it)
                                    uriString = if (downloadStatus is DownloadStatus.Downloaded) {
                                        downloadStatus.uriString
                                    } else {
                                        episode.httpsMp3Url
                                    }
                                } ?: run {
                                    uriString = episode.httpsMp3Url
                                }
                            },
                    listened = db.listenedDao().findById(episodeId)))
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

    suspend fun fetchRelatedLinks(episodeId: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.getEpisodeRelatedLinksAsync(episodeId).await() }
        val relatedLinks = response?.body()
        if (response?.isSuccessful == true && relatedLinks != null) {
            Resource.Success(relatedLinks)
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

    suspend fun addRelatedLink(episodeId: String, title: String, url: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.addEpisodeRelatedLinkAsync(episodeId, title, url).await() }
        val addCommentResponse = response?.body()
        if (response?.isSuccessful == true) {
            Resource.Success(addCommentResponse != null)
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

    suspend fun markEpisodeAsListened(episodeId: String) = withContext(Dispatchers.IO) {
        val response = safeApiCall { api.markEpisodeAsListenedAsync(episodeId).await() }
        val addListenedResponse = response?.body()
        if (response?.isSuccessful == true) {
            Resource.Success(addListenedResponse != null)
        } else {
            Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
        }
    }

    suspend fun addDownload(episodeId: String, downloadId: Long) = withContext(Dispatchers.IO) {
        db.downloadDao().insert(Download(episodeId, downloadId))
    }

    suspend fun deleteDownload(episode: Episode) = withContext(Dispatchers.IO) {
        val download = db.downloadDao().findById(episode._id)

        download?.let {
            // Delete DB entry.
            db.downloadDao().delete(download)

            // Delete local file
            try {
                downloadManager.deleteDownload(episode._id)
            } catch (e: Exception) {
                // Ignore delete errors and log it to Crashlytics.

                Timber.e(e)
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