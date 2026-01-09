package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity

@Dao
interface TaskDao {
    //--------------Lesson------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lessonEntity: TaskLessonEntity)
    @Update
    suspend fun updateLesson(lessonEntity: TaskLessonEntity)
    @Delete
    suspend fun deleteLesson(lessonEntity: TaskLessonEntity)

    //--------------Exam-------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(examEntity: TaskExamEntity)
    @Update
    suspend fun updateExam(examEntity: TaskExamEntity)
    @Delete
    suspend fun deleteExam(examEntity: TaskExamEntity)

    //--------------Homework-------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homeworkEntity: TaskHomeworkEntity)
    @Update
    suspend fun updateHomework(homeworkEntity: TaskHomeworkEntity)
    @Delete
    suspend fun deleteHomework(homeworkEntity: TaskHomeworkEntity)
}