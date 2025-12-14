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
    fun getCourseById(id: String): Flow<CourseEntity>

    @Query("DELETE FROM courses WHERE program_table_id = :id")
    suspend fun deleteCoursesByProgramTableId(id: String)

    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE program_table_id = :id")
    fun getCoursesByProgramTableId(id: String): Flow<List<CourseEntity>>
}