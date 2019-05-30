package com.koalatea.sedaily.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koalatea.sedaily.model.Episode

@Dao
interface EpisodeDao {

    @get:Query("SELECT * FROM episode")
    val all: List<Episode>

    @Query("SELECT * FROM episode WHERE _id = :id LIMIT 1")
    fun findById(id: String): Episode

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg episodes: Episode)

    @Query("DELETE FROM episode")
    fun clearTable()

}