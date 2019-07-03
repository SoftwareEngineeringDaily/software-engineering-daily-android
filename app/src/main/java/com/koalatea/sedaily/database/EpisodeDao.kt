package com.koalatea.sedaily.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koalatea.sedaily.database.model.Episode
import com.koalatea.sedaily.database.model.EpisodeDetails

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episode " +
            "LEFT JOIN listened ON listened.postId = episode._id " +
            "WHERE episode.searchQueryHashCode = :searchQueryHashCode " +
            "ORDER BY episode.indexInResponse ASC")
    fun getEpisodesBySearchQuery(searchQueryHashCode: Int): DataSource.Factory<Int, EpisodeDetails>

    @Query("SELECT MAX(indexInResponse) + 1 FROM episode WHERE searchQueryHashCode = :searchQueryHashCode")
    fun getNextIndexBySearchQuery(searchQueryHashCode: Int): Int

    @Query("SELECT * FROM episode " +
            "LEFT JOIN listened ON listened.postId = episode._id " +
            "WHERE episode._id = :id LIMIT 1")
    suspend fun findById(id: String): EpisodeDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(episodes: List<Episode>)

    @Query("UPDATE episode SET upvoted = :newState, score = :newScore WHERE _id = :id")
    suspend fun vote(id: String, newState: Boolean, newScore: Int)

    @Query("UPDATE episode SET bookmarked = :newState WHERE _id = :id")
    suspend fun bookmark(id: String, newState: Boolean)

    @Query("DELETE FROM episode WHERE searchQueryHashCode = :searchQueryHashCode")
    fun deleteBySearchQuery(searchQueryHashCode: Int)

    @Query("DELETE FROM episode")
    fun clearTable()

}