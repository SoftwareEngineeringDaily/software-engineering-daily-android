package com.koalatea.sedaily.repository

import androidx.annotation.MainThread
import androidx.paging.toLiveData
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails
import com.koalatea.sedaily.downloader.DownloadManager
import com.koalatea.sedaily.feature.episodes.paging.EpisodesBoundaryCallback
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.*
import com.koalatea.sedaily.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class EpisodesRepository(
        private val api: SEDailyApi,
        private val db: AppDatabase,
        private val networkManager: NetworkManager,
        private val downloadManager: DownloadManager,
        private val sessionRepository: SessionRepository
) {

    @MainThread
    fun fetchEpisodes(searchQuery: SearchQuery, pageSize: Int = 20): PagedResult<EpisodeDetails> {
        val networkPageSize = pageSize * 2

        val boundaryCallback = EpisodesBoundaryCallback(
                searchQuery = searchQuery,
                api = api,
                networkManager = networkManager,
                insertResultIntoDb = this::insertResultIntoDb,
                handleSuccessfulRefresh = this::handleSuccessfulRefresh,
                networkPageSize = networkPageSize)

        val livePagedList = db.episodeDao().getEpisodesBySearchQuery(searchQuery.hashCode()).toLiveData(
                pageSize = pageSize,
                boundaryCallback = boundaryCallback)

        // Load first form DB then try refreshing
        boundaryCallback.refresh()

        return PagedResult(
                pagedList = livePagedList,
                networkState = boundaryCallback.networkState,
                refresh = { boundaryCallback.refresh() }
        )
    }

    suspend fun fetchBookmarks() = withContext(Dispatchers.IO) {
        if (sessionRepository.isLoggedIn) {
            val response = safeApiCall { api.getBookmarksAsync().await() }
            val bookmarks = response?.body()
            if (response?.isSuccessful == true && bookmarks != null) {
                Resource.Success(bookmarks)
            } else {
                Resource.Error(response?.errorBody().toException(), networkManager.isConnected)
            }
        } else {
            Resource.RequireLogin
        }
    }

    // TODO :: Return Resource instead
    suspend fun vote(episodeId: String, originalState: Boolean, originalScore: Int) = withContext(Dispatchers.IO) {
        val response = if (originalState) {
            db.episodeDao().vote(episodeId, !originalState, originalScore - 1)

            safeApiCall { api.downvoteEpisodeAsync(episodeId).await() }
        } else {
            db.episodeDao().vote(episodeId, !originalState, originalScore + 1)

            safeApiCall { api.upvoteEpisodeAsync(episodeId).await() }
        }

        // Revert UI changes back.
        if (response?.isSuccessful == false || response?.body() == null) {
            db.episodeDao().vote(episodeId, originalState, originalScore)

            return@withContext false
        }

        return@withContext true
    }

    // TODO :: Return Resource instead
    suspend fun bookmark(episodeId: String, originalState: Boolean) = withContext(Dispatchers.IO) {
        // Update UI right away.
        db.episodeDao().bookmark(episodeId, !originalState)

        val response = if (originalState) {
            safeApiCall { api.unfavoriteEpisodeAsync(episodeId).await() }
        } else {
            safeApiCall { api.favoriteEpisodeAsync(episodeId).await() }
        }

        // Revert UI changes back.
        if (response?.isSuccessful == false || response?.body() == null) {
            db.episodeDao().bookmark(episodeId, originalState)
            return@withContext false
        }

        return@withContext true
    }

    suspend fun clearLocalCache(searchQuery: SearchQuery) = withContext(Dispatchers.IO) {
        db.episodeDao().deleteBySearchQuery(searchQuery.hashCode())
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        db.episodeDao().clearTable()
        db.listenedDao().clearTable()

        db.downloadDao().getAll().forEach {
            try {
                downloadManager.deleteDownload(it.postId)
            } catch (e: Exception) {
                // Ignore delete errors and log it to Crashlytics.

                Timber.e(e)
            }
        }
        db.downloadDao().clearTable()
    }

    @MainThread
    private fun handleSuccessfulRefresh(searchQuery: SearchQuery, episodes: List<Episode>?) {
        GlobalScope.launch(Dispatchers.IO) {
            db.runInTransaction {
                db.episodeDao().deleteBySearchQuery(searchQuery.hashCode())

                insertResultIntoDb(searchQuery, episodes)
            }
        }
    }

    @MainThread
    private fun insertResultIntoDb(searchQuery: SearchQuery, episodes: List<Episode>?) {
        GlobalScope.launch(Dispatchers.IO) {
            episodes?.let { episodes ->
                val searchQueryHashCode = searchQuery.hashCode()
                val start = db.episodeDao().getNextIndexBySearchQuery(searchQueryHashCode)
                val items = episodes.mapIndexed { index, child ->
                    child.searchQueryHashCode = searchQueryHashCode
                    child.indexInResponse = start + index
                    child
                }

                db.episodeDao().insert(items)
            }
        }
    }

}