package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity)

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules WHERE course_id = :id")
    fun getSchedulesByCourseId(id: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE program_table_id = :id")
    fun getSchedulesByProgramTableId(id: String): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules WHERE course_id = :id")
    suspend fun deleteSchedulesByCourseId(id: String)

    @Query("DELETE FROM schedules WHERE program_table_id = :id")
    suspend fun deleteSchedulesByProgramTableId(id: String)

}