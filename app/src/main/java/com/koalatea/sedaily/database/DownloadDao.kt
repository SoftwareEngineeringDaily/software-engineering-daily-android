package com.koalatea.sedaily.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.koalatea.sedaily.database.model.Download

@Dao
interface DownloadDao {

    @get:Query("SELECT * FROM download")
    val all: List<Download>

    @Query("SELECT * FROM download WHERE postId = :id LIMIT 1")
    suspend fun findById(id: String): Download?

    @Insert(onConflict = REPLACE)
    suspend fun insert(download: Download)

    @Delete
    fun delete(download: Download)

}