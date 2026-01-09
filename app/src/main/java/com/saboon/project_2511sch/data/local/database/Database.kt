package com.saboon.project_2511sch.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.ScheduleDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.data.local.entity.ScheduleEntity

@Database(
    entities = [
        ProgramTableEntity::class,
        CourseEntity::class,
        ScheduleEntity::class,
        FileEntity::class
    ],
//    autoMigrations = [AutoMigration(from = 1, to = 2, spec = com.saboon.project_2511sch.data.local.database.Database.AutoMigration_1_2::class)],
    version = 1,
    exportSchema = true)
abstract class Database(): RoomDatabase() {

//    class AutoMigration_1_2 : AutoMigrationSpec

    abstract fun programDao(): ProgramTableDao
    abstract fun courseDao(): CourseDao
    abstract fun scheduleDao(): ScheduleDao

    abstract fun fileDao(): FileDao
}