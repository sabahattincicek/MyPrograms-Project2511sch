package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImp @Inject constructor(
    private val taskDao: TaskDao,
    private val fileDao: FileDao
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
            fileDao.deleteAllByTaskId(task.id)
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

    override fun getAllTasksByCourseId(id: String): Flow<Resource<List<Task>>> {
        return combine<List<TaskLessonEntity>, List<TaskExamEntity>, List<TaskHomeworkEntity>, Resource<List<Task>>>(
            taskDao.getAllLessonsByCourseId(id),
            taskDao.getAllExamsByCourseId(id),
            taskDao.getAllHomeworksByCourseId(id)
        ) { lessons, exams, homeworks ->
            // Convert entities to domain models and combine into one list
            val allTasks = mutableListOf<Task>()
            allTasks.addAll(lessons.map { it.toDomain() })
            allTasks.addAll(exams.map { it.toDomain() })
            allTasks.addAll(homeworks.map { it.toDomain() })

            // Optionally sort by date/time if needed
            // allTasks.sortBy { it.date }

            Resource.Success(allTasks.toList())
        }.catch { e ->
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override fun getAllTasksByCourseIds(ids: List<String>): Flow<Resource<List<Task>>> {
        return combine<List<TaskLessonEntity>, List<TaskExamEntity>, List<TaskHomeworkEntity>, Resource<List<Task>>>(
            taskDao.getAllLessonsByCourseIds(ids),
            taskDao.getAllExamsByCourseIds(ids),
            taskDao.getAllHomeworksByCourseIds(ids)
        ) { lessons, exams, homeworks ->
            // Convert entities to domain models and combine into one list
            val allTasks = mutableListOf<Task>()
            allTasks.addAll(lessons.map { it.toDomain() })
            allTasks.addAll(exams.map { it.toDomain() })
            allTasks.addAll(homeworks.map { it.toDomain() })

            // Optionally sort by date/time if needed
            // allTasks.sortBy { it.date }

            Resource.Success(allTasks.toList())
        }.catch { e ->
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override fun getAllTaskByProgramTableId(id: String): Flow<Resource<List<Task>>> {
        return combine<List<TaskLessonEntity>, List<TaskExamEntity>, List<TaskHomeworkEntity>, Resource<List<Task>>>(
            taskDao.getAllLessonsByProgramTableId(id),
            taskDao.getAllExamsByProgramTableId(id),
            taskDao.getAllHomeworksByProgramTableId(id)
        ) { lessonEntities, examEntities, homeworkEntities ->
            val allTasks = mutableListOf<Task>()
            allTasks.addAll(lessonEntities.map { it.toDomain() })
            allTasks.addAll(examEntities.map { it.toDomain() })
            allTasks.addAll(homeworkEntities.map { it.toDomain() })

            // Optionally sort by date/time if needed
            // allTasks.sortBy { it.date }

            Resource.Success(allTasks.toList())
        }.catch { e ->
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override fun getAllTasksByProgramTableIds(ids: List<String>): Flow<Resource<List<Task>>> {
        return combine<List<TaskLessonEntity>, List<TaskExamEntity>, List<TaskHomeworkEntity>, Resource<List<Task>>>(
            taskDao.getAllLessonsByProgramTableIds(ids),
            taskDao.getAllExamsByProgramTableIds(ids),
            taskDao.getAllHomeworksByProgramTableIds(ids)
        ) { lessonEntities, examEntities, homeworkEntities ->
            val allTasks = mutableListOf<Task>()
            allTasks.addAll(lessonEntities.map { it.toDomain() })
            allTasks.addAll(examEntities.map { it.toDomain() })
            allTasks.addAll(homeworkEntities.map { it.toDomain() })

            // Optionally sort by date/time if needed
            // allTasks.sortBy { it.date }

            Resource.Success(allTasks.toList())
        }.catch { e ->
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}