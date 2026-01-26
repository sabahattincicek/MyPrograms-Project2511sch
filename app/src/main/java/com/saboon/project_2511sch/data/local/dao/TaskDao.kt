package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity
import com.saboon.project_2511sch.domain.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //--------------Lesson------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lessonEntity: TaskLessonEntity)
    @Update
    suspend fun updateLesson(lessonEntity: TaskLessonEntity)
    @Delete
    suspend fun deleteLesson(lessonEntity: TaskLessonEntity)
    @Query("SELECT * FROM task_lessons WHERE course_id = :id")
    fun getAllLessonsByCourseId(id: String): Flow<List<TaskLessonEntity>>
    @Query("SELECT * FROM task_lessons WHERE is_active = 1 AND course_id IN (:ids)")
    fun getAllLessonsByCourseIds(ids: List<String>): Flow<List<TaskLessonEntity>>
    @Query("SELECT * FROM task_lessons WHERE program_table_id = :id")
    fun getAllLessonsByProgramTableId(id: String): Flow<List<TaskLessonEntity>>
    @Query("SELECT * FROM task_lessons WHERE program_table_id IN (:ids)")
    fun getAllLessonsByProgramTableIds(ids: List<String>): Flow<List<TaskLessonEntity>>

    //--------------Exam-------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(examEntity: TaskExamEntity)
    @Update
    suspend fun updateExam(examEntity: TaskExamEntity)
    @Delete
    suspend fun deleteExam(examEntity: TaskExamEntity)
    @Query("SELECT * FROM task_exams WHERE course_id = :id")
    fun getAllExamsByCourseId(id: String): Flow<List<TaskExamEntity>>
    @Query("SELECT * FROM task_exams WHERE is_active = 1 AND course_id IN (:ids)")
    fun getAllExamsByCourseIds(ids: List<String>): Flow<List<TaskExamEntity>>
    @Query("SELECT * FROM task_exams WHERE program_table_id = :id")
    fun getAllExamsByProgramTableId(id: String): Flow<List<TaskExamEntity>>
    @Query("SELECT * FROM task_exams WHERE program_table_id IN (:ids)")
    fun getAllExamsByProgramTableIds(ids: List<String>): Flow<List<TaskExamEntity>>

    //--------------Homework-------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homeworkEntity: TaskHomeworkEntity)
    @Update
    suspend fun updateHomework(homeworkEntity: TaskHomeworkEntity)
    @Delete
    suspend fun deleteHomework(homeworkEntity: TaskHomeworkEntity)
    @Query("SELECT * FROM task_homeworks WHERE course_id = :id")
    fun getAllHomeworksByCourseId(id: String): Flow<List<TaskHomeworkEntity>>
    @Query("SELECT * FROM task_homeworks WHERE is_active = 1 AND course_id IN (:ids)")
    fun getAllHomeworksByCourseIds(ids: List<String>): Flow<List<TaskHomeworkEntity>>
    @Query("SELECT * FROM task_homeworks WHERE program_table_id = :id")
    fun getAllHomeworksByProgramTableId(id: String): Flow<List<TaskHomeworkEntity>>
    @Query("SELECT * FROM task_homeworks WHERE program_table_id IN (:ids)")
    fun getAllHomeworksByProgramTableIds(ids: List<String>): Flow<List<TaskHomeworkEntity>>
}