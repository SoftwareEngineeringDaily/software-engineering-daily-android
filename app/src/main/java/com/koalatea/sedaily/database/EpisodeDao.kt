package com.koalatea.sedaily.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koalatea.sedaily.database.model.Episode

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episode ORDER BY indexInResponse ASC")
    suspend fun getEpisodes(): List<Episode>

    @Query("SELECT * FROM episode WHERE searchQueryHashCode = :searchQueryHashCode ORDER BY indexInResponse ASC")
    fun getEpisodesBySearchQuery(searchQueryHashCode: Int) : DataSource.Factory<Int, Episode>

    @Query("SELECT MAX(indexInResponse) + 1 FROM episode WHERE searchQueryHashCode = :searchQueryHashCode")
    fun getNextIndexBySearchQuery(searchQueryHashCode: Int) : Int

    @Query("SELECT * FROM episode WHERE _id = :id LIMIT 1")
    suspend fun findById(id: String): Episode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(episodes: List<Episode>)

    @Query("UPDATE episode SET upvoted = :newState, score = :newScore WHERE _id = :id")
    suspend fun vote(id: String, newState: Boolean, newScore: Int)

    @Query("UPDATE episode SET bookmarked = :newState WHERE _id = :id")
    suspend fun bookmark(id: String, newState: Boolean)

    @Query("DELETE FROM episode WHERE searchQueryHashCode = :searchQueryHashCode")
    fun deleteBySearchQuery(searchQueryHashCode: Int)
    
    @Query("DELETE FROM episode")
    suspend fun clearTable()

}