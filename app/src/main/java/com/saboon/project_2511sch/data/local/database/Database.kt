package com.saboon.project_2511sch.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity

@Database(
    entities = [
        ProgramTableEntity::class,
        CourseEntity::class,
        FileEntity::class,
        TaskLessonEntity::class,
        TaskExamEntity::class,
        TaskHomeworkEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true)
abstract class Database(): RoomDatabase() {
    abstract fun programDao(): ProgramTableDao
    abstract fun courseDao(): CourseDao
    abstract fun fileDao(): FileDao
    abstract fun taskDao(): TaskDao
}