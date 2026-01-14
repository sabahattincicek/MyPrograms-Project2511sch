package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(task: Task): Resource<Task>{
        val updatedTask = when(task) {
            is Task.Exam -> task.copy(
                updatedAt = System.currentTimeMillis(),
                version = task.version + 1
            )
            is Task.Homework -> task.copy(
                updatedAt = System.currentTimeMillis(),
                version = task.version + 1
            )
            is Task.Lesson -> task.copy(
                updatedAt = System.currentTimeMillis(),
                version = task.version + 1
            )
        }
        return taskRepository.updateTask(updatedTask)
    }
}