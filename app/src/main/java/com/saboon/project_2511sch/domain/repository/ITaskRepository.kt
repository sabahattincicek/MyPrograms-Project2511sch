package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ITaskRepository {
    suspend fun insertTask(task: Task): Resource<Task>
    suspend fun updateTask(task: Task): Resource<Task>
    suspend fun deleteTask(task: Task): Resource<Task>
    fun getAllTasksByCourseId(id: String): Flow<Resource<List<Task>>>
}