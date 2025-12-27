package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IScheduleRepository {
    suspend fun insertSchedule(task: Task): Resource<Task>

    suspend fun updateSchedule(task: Task): Resource<Task>

    suspend fun deleteSchedule(task: Task): Resource<Task>

    fun getSchedulesByCourseId(id: String): Flow<Resource<List<Task>>>

    fun getSchedulesByProgramTableId(id: String) : Flow<Resource<List<Task>>>

    suspend fun deleteSchedulesByCourseId(id: String): Resource<Unit>

    suspend fun deleteSchedulesByProgramTableId(id: String): Resource<Unit>
}