package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tagEntity: TagEntity)
    @Update
    suspend fun update(tagEntity: TagEntity)
    @Delete
    suspend fun delete(tagEntity: TagEntity)
    @Query("UPDATE tags SET is_active = :isActive WHERE id = :id")
    suspend fun activationById(id: String, isActive: Boolean)
    @Query("SELECT * FROM tags WHERE id = :id")
    fun getById(id: String): Flow<TagEntity>
    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<TagEntity>>
    @Query("SELECT * FROM tags WHERE is_active = 1")
    fun getAllActive(): Flow<List<TagEntity>>
}