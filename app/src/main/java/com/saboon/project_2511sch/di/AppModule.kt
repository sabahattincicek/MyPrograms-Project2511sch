package com.saboon.project_2511sch.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saboon.project_2511sch.data.alarm.AlarmSchedulerImp
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.database.Database
import com.saboon.project_2511sch.data.repository.CourseRepositoryImp
import com.saboon.project_2511sch.data.repository.ProgramTableRepositoryImp
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    val MIGRATION_3_4 = object: Migration(3,4) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("DROP TABLE IF EXISTS files")
//            database.execSQL("ALTER TABLE program_tables ADD COLUMN files")
//            database.execSQL("ALTER TABLE courses ADD COLUMN files")
//            database.execSQL("ALTER TABLE task_lessons ADD COLUMN files")
//            database.execSQL("ALTER TABLE task_exams ADD COLUMN files")
//            database.execSQL("ALTER TABLE task_homeworks ADD COLUMN files")
//        }
//    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        Database::class.java,
        "Project2511schDatabase"
    ).build()

    @Singleton
    @Provides
    fun provideProgramTableDao(database: Database) = database.programDao()

    @Singleton
    @Provides
    fun provideCourseDao(database: Database) = database.courseDao()

    @Singleton
    @Provides
    fun provideTaskDao(database: Database) = database.taskDao()

    @Singleton
    @Provides
    fun provideSFileDao(database: Database) = database.sFileDao()

}