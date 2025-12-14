package com.saboon.project_2511sch.di

import android.content.Context
import com.saboon.project_2511sch.data.alarm.AlarmSchedulerImp
import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.dao.ScheduleDao
import com.saboon.project_2511sch.data.local.dao.UserDao
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
    fun provideUserRepositoryImp(userDao: UserDao): IUserRepository{
        return UserRepositoryImp(userDao)
    }

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
    fun provideScheduleRepositoryImp(scheduleDao: ScheduleDao): IScheduleRepository {
        return ScheduleRepositoryImp(scheduleDao)
    }

    @Singleton
    @Provides
    fun provideAuthRepositoryImp(userRepository: IUserRepository): IAuthRepository {
        return AuthRepositoryImp(userRepository)
    }

    @Singleton
    @Provides
    fun provideAlarmScheduler(@ApplicationContext context: Context): IAlarmScheduler{
        return AlarmSchedulerImp(context)
    }
    

}