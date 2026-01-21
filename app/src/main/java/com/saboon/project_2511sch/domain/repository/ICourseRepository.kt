package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface ICourseRepository {

    suspend fun insertCourse(course: Course): Resource<Course>

    suspend fun deleteCourse(course: Course): Resource<Course>

    suspend fun updateCourse(course: Course): Resource<Course>

    fun getCourseById(id: String): Flow<Resource<Course>>

    suspend fun deleteCoursesByProgramTableId(id: String): Resource<Unit>

    fun getAllCourses(): Flow<Resource<List<Course>>>

    fun getCoursesByProgramTableId(id: String): Flow<Resource<List<Course>>>
    fun getAllCoursesByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>>


}