package com.saboon.project_2511sch.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.ScheduleDao
import com.saboon.project_2511sch.data.local.dao.UserDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.data.local.entity.ScheduleEntity
import com.saboon.project_2511sch.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProgramTableEntity::class,
        CourseEntity::class,
        ScheduleEntity::class,
        FileEntity::class
    ],
    version = 2,
    exportSchema = false)
abstract class Database(): RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun programDao(): ProgramTableDao
    abstract fun courseDao(): CourseDao
    abstract fun scheduleDao(): ScheduleDao

    abstract fun fileDao(): FileDao
}