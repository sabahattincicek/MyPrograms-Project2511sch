package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface ICourseRepository {

    suspend fun insert(course: Course): Resource<Course>

    suspend fun delete(course: Course): Resource<Course>

    suspend fun update(course: Course): Resource<Course>
    suspend fun activationById(id: String, isActive: Boolean): Resource<Unit>

    fun getById(id: String): Flow<Resource<Course>>

    suspend fun deleteByProgramTableId(id: String): Resource<Unit>

    fun getAll(): Flow<Resource<List<Course>>>

    fun getAllByProgramTableId(id: String): Flow<Resource<List<Course>>>
    fun getAllByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>>
    fun getAllActivesByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>>
    suspend fun getAllCount(): Resource<Int>
    suspend fun getAllActiveCount(): Resource<Int>

}