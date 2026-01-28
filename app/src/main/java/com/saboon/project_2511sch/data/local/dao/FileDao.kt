package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: FileEntity)

    @Delete
    suspend fun delete(file: FileEntity)

    @Update
    suspend fun update(file: FileEntity)

    @Query("SELECT * FROM files WHERE task_id = :id")
    fun getAllByTaskId(id: String): Flow<List<FileEntity>>
    @Query("SELECT * FROM files WHERE course_id = :id")
    fun getAllByCourseId(id: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE program_table_id = :id")
    fun getAllByProgramTableId(id: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files")
    fun getAll(): Flow<List<FileEntity>>
}