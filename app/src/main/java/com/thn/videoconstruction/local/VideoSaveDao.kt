package com.thn.videoconstruction.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.thn.videoconstruction.models.VideoSaves
@Dao
interface VideoSaveDao {
    @Query("SELECT * FROM note_table")
    fun getAll(): LiveData<List<VideoSaves>>

    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getById(id: Int): VideoSaves?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insert(videoSaves: VideoSaves)

    @Update
    suspend fun update(videoSaves: VideoSaves)

    @Delete
    fun delete(videoSaves: VideoSaves)
}
