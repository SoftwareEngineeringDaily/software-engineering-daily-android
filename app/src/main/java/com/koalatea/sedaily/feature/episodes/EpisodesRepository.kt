package com.koalatea.sedaily.feature.episodes

import androidx.annotation.MainThread
import androidx.paging.toLiveData
import com.koalatea.sedaily.database.AppDatabase
import com.koalatea.sedaily.feature.episodes.paging.EpisodesBoundaryCallback
import com.koalatea.sedaily.model.Episode
import com.koalatea.sedaily.model.SearchQuery
import com.koalatea.sedaily.network.Result
import com.koalatea.sedaily.network.SEDailyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodesRepository(
        private val api: SEDailyApi,
        private val db: AppDatabase) {

    @MainThread
    fun fetchPosts(searchQuery: SearchQuery, pageSize: Int = 20): Result<Episode> {
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

        return Result(
                pagedList = livePagedList,
                networkState = boundaryCallback.networkState,
                refresh = { boundaryCallback.refresh() },
                refreshState = boundaryCallback.refreshState
        )
    }

    @MainThread
    fun upvote(episodeId: String, originalState: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            // Update UI right away.
            db.episodeDao().upvote(episodeId, !originalState)

            val response = if (originalState) {
                api.downvoteEpisodeAsync(episodeId).await()
            } else {
                api.upvoteEpisodeAsync(episodeId).await()
            }

            // Revert UI changes back.
            if (!response.isSuccessful || response.body() == null) {
                db.episodeDao().upvote(episodeId, originalState)
            }
        }
    }

    @MainThread
    fun bookmark(episodeId: String, originalState: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
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
            }
        }
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
                db.runInTransaction {
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

}