package com.saboon.project_2511sch.di

import android.content.Context
import androidx.room.Room
import com.saboon.project_2511sch.data.alarm.AlarmSchedulerImp
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.ScheduleDao
import com.saboon.project_2511sch.data.local.dao.UserDao
import com.saboon.project_2511sch.data.local.database.Database
import com.saboon.project_2511sch.data.repository.AuthRepositoryImp
import com.saboon.project_2511sch.data.repository.CourseRepositoryImp
import com.saboon.project_2511sch.data.repository.ProgramTableRepositoryImp
import com.saboon.project_2511sch.data.repository.ScheduleRepositoryImp
import com.saboon.project_2511sch.data.repository.UserRepositoryImp
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.repository.IAuthRepository
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.domain.repository.IUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        Database::class.java,
        "Project2511schDatabase"
    ).build()

    @Singleton
    @Provides
    fun provideUserDao(database: Database) = database.userDao()

    @Singleton
    @Provides
    fun provideProgramTableDao(database: Database) = database.programDao()

    @Singleton
    @Provides
    fun provideCourseDao(database: Database) = database.courseDao()

    @Singleton
    @Provides
    fun provideScheduleDao(database: Database) = database.scheduleDao()

    @Singleton
    @Provides
    fun provideFileDao(database: Database) = database.fileDao()

}