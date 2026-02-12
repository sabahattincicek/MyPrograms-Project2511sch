package com.saboon.project_2511sch.di

import android.content.Context
import com.saboon.project_2511sch.data.alarm.AlarmSchedulerImp
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.SFileDao
import com.saboon.project_2511sch.data.local.dao.TaskDao
import com.saboon.project_2511sch.data.repository.CourseRepositoryImp
import com.saboon.project_2511sch.data.repository.ProgramTableRepositoryImp
import com.saboon.project_2511sch.data.repository.SFileRepositoryImp
import com.saboon.project_2511sch.data.repository.TaskRepositoryImp
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideProgramTableRepositoryImp(programTableDao: ProgramTableDao): IProgramTableRepository {
        return ProgramTableRepositoryImp(programTableDao)
    }

    @Singleton
    @Provides
    fun provideCourseRepositoryImp(courseDao: CourseDao): ICourseRepository {
        return CourseRepositoryImp(courseDao)
    }
    @Singleton
    @Provides
    fun provideAlarmScheduler(@ApplicationContext context: Context): IAlarmScheduler{
        return AlarmSchedulerImp(context)
    }

    @Singleton
    @Provides
    fun provideTaskRepositoryImp(taskDao: TaskDao): ITaskRepository {
        return TaskRepositoryImp(taskDao)
    }

    @Singleton
    @Provides
    fun provideSFileRepositoryImp(@ApplicationContext context: Context, sFileDao: SFileDao): ISFileRepository{
        return SFileRepositoryImp(context, sFileDao)
    }

}