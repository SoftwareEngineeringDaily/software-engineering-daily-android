package com.koalatea.sedaily.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.koalatea.sedaily.database.model.Listened

@Dao
interface ListenedDao {

    @Query("SELECT * FROM listened WHERE postId = :id LIMIT 1")
    suspend fun findById(id: String): Listened?

    @Insert(onConflict = REPLACE)
    suspend fun insert(listened: Listened)

    @Delete
    suspend fun delete(listened: Listened)

}