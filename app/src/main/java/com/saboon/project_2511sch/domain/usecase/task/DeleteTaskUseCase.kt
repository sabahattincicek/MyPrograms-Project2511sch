package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(task: Task): Resource<Task>{
        return taskRepository.deleteTask(task)
    }
}