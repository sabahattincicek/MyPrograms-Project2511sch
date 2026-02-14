package com.saboon.project_2511sch.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.SFileDao
import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.data.local.entity.SFileEntity
import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity
import com.saboon.project_2511sch.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProgramTableEntity::class,
        CourseEntity::class,
        TaskLessonEntity::class,
        TaskExamEntity::class,
        TaskHomeworkEntity::class,
        SFileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class Database(): RoomDatabase() {
    abstract fun programDao(): ProgramTableDao
    abstract fun courseDao(): CourseDao
    abstract fun taskDao(): TaskDao
    abstract fun sFileDao(): SFileDao
}