package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class TaskRepositoryImp @Inject constructor(
    private val taskDao: TaskDao
): ITaskRepository {
    override suspend fun insertTask(task: Task): Resource<Task> {
        try {
            when(task) {
                is Task.Exam -> taskDao.insertExam(task.toEntity())
                is Task.Homework -> taskDao.insertHomework(task.toEntity())
                is Task.Lesson -> taskDao.insertLesson(task.toEntity())
            }
            return Resource.Success(task)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun updateTask(task: Task): Resource<Task> {
        try {
            when(task) {
                is Task.Lesson -> taskDao.updateLesson(task.toEntity())
                is Task.Exam -> taskDao.updateExam(task.toEntity())
                is Task.Homework -> taskDao.updateHomework(task.toEntity())
            }
            return Resource.Success(task)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun deleteTask(task: Task): Resource<Task> {
        try{
            when(task) {
                is Task.Lesson -> taskDao.deleteLesson(task.toEntity())
                is Task.Exam -> taskDao.deleteExam(task.toEntity())
                is Task.Homework -> taskDao.deleteHomework(task.toEntity())
            }
            return Resource.Success(task)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }


}