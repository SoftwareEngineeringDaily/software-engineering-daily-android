package com.koalatea.sedaily.feature.episodes

import androidx.annotation.MainThread
import androidx.paging.toLiveData
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.feature.episodes.paging.EpisodesBoundaryCallback
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesRepository(
        private val api: SEDailyApi,
        private val db: AppDatabase) {

    @MainThread
    fun fetchEpisodes(searchQuery: SearchQuery, pageSize: Int = 20): Result<Episode> {
        val networkPageSize = pageSize * 2

        val boundaryCallback = EpisodesBoundaryCallback(
                searchQuery = searchQuery,
                api = api,
                insertResultIntoDb = this::insertResultIntoDb,
                handleSuccessfulRefresh = this::handleSuccessfulRefresh,
                networkPageSize = networkPageSize)

        val livePagedList = db.episodeDao().getEpisodesBySearchQuery(searchQuery.hashCode()).toLiveData(
                pageSize = pageSize,
                boundaryCallback = boundaryCallback)

        // Load first form DB then try refreshing
        boundaryCallback.refresh()

        return Result(
                pagedList = livePagedList,
                networkState = boundaryCallback.networkState,
                refresh = { boundaryCallback.refresh() },
                refreshState = boundaryCallback.refreshState
        )
    }

    @MainThread
    suspend fun clearLocalCache(searchQuery: SearchQuery)  = withContext(Dispatchers.IO) {
        db.episodeDao().deleteBySearchQuery(searchQuery.hashCode())
    }

    suspend fun vote(episodeId: String, originalState: Boolean, originalScore: Int) = withContext(Dispatchers.IO) {
        val response = if (originalState) {
            db.episodeDao().vote(episodeId, !originalState, originalScore - 1)

            api.downvoteEpisodeAsync(episodeId).await()
        } else {
            db.episodeDao().vote(episodeId, !originalState, originalScore + 1)

            api.upvoteEpisodeAsync(episodeId).await()
        }

        // Revert UI changes back.
        if (!response.isSuccessful || response.body() == null) {
            db.episodeDao().vote(episodeId, originalState, originalScore)

            return@withContext false
        }

        return@withContext true
    }

    suspend fun bookmark(episodeId: String, originalState: Boolean) = withContext(Dispatchers.IO) {
        // Update UI right away.
        db.episodeDao().bookmark(episodeId, !originalState)

        val response = if (originalState) {
            api.unfavoriteEpisodeAsync(episodeId).await()
        } else {
            api.favoriteEpisodeAsync(episodeId).await()
        }

        // Revert UI changes back.
        if (!response.isSuccessful || response.body() == null) {
            db.episodeDao().bookmark(episodeId, originalState)
            return@withContext false
        }

        return@withContext true
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