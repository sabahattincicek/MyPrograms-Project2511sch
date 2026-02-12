package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.SFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sFile: SFileEntity)
    @Update
    suspend fun update(sFile: SFileEntity)
    @Delete
    suspend fun delete(sFile: SFileEntity)
    @Query("SELECT * FROM s_files")
    fun getAll(): Flow<List<SFileEntity>>
}