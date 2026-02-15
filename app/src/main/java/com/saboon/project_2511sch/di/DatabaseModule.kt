package com.saboon.project_2511sch.di

import android.content.Context
import androidx.room.Room
import com.saboon.project_2511sch.data.local.database.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        Database::class.java,
        "Project2511schDatabase"
    ).fallbackToDestructiveMigration(true) // TODO: DELETE THIS WHEN RELEASE
        .build()

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
    fun provideTaskDao(database: Database) = database.taskDao()

    @Singleton
    @Provides
    fun provideSFileDao(database: Database) = database.sFileDao()

}