package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.Schedule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IScheduleRepository {
    suspend fun insertSchedule(schedule: Schedule): Resource<Schedule>

    suspend fun updateSchedule(schedule: Schedule): Resource<Schedule>

    suspend fun deleteSchedule(schedule: Schedule): Resource<Schedule>

    fun getSchedulesByCourseId(id: String): Flow<Resource<List<Schedule>>>

    fun getSchedulesByProgramTableId(id: String) : Flow<Resource<List<Schedule>>>

    suspend fun deleteSchedulesByCourseId(id: String): Resource<Unit>

    suspend fun deleteSchedulesByProgramTableId(id: String): Resource<Unit>
}