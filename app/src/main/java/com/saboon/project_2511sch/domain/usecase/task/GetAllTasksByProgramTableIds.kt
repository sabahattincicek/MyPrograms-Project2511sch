package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTasksByProgramTableIds @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    operator fun invoke(ids: List<String>): Flow<Resource<List<Task>>>{
        return taskRepository.getAllTasksByProgramTableIds(ids)
    }
}