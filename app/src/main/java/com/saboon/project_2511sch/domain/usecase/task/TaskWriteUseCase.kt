package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class TaskWriteUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend fun insert(task: Task): Resource<Task>{
        return taskRepository.insert(task)
    }
    suspend fun update(task: Task): Resource<Task>{
        val updatedTask = when(task) {
            is Task.Exam -> task.copy(
                version = task.version + 1,
                updatedAt = System.currentTimeMillis(),
            )
            is Task.Homework -> task.copy(
                version = task.version + 1,
                updatedAt = System.currentTimeMillis(),
            )
            is Task.Lesson -> task.copy(
                version = task.version + 1,
                updatedAt = System.currentTimeMillis(),
            )
        }
        return taskRepository.updateTask(updatedTask)
    }
    suspend fun delete(task: Task): Resource<Task>{
        return taskRepository.deleteTask(task)
    }
}