package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity)
    @Delete
    suspend fun delete(course: CourseEntity)
    @Update
    suspend fun update(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getById(id: String): Flow<CourseEntity>

    @Query("DELETE FROM courses WHERE program_table_id = :id")
    suspend fun deleteByProgramTableId(id: String)

    @Query("SELECT * FROM courses")
    fun getAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE program_table_id = :id")
    fun getAllByProgramTableId(id: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE program_table_id IN (:ids)")
    fun getAllByProgramTableIds(ids: List<String>): Flow<List<CourseEntity>>
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getAllCount(): Int
    @Query("SELECT COUNT(*) FROM courses WHERE is_active = 1")
    suspend fun getAllActiveCount(): Int
}