package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseReadUseCase @Inject constructor(
    private val courseRepository: ICourseRepository
) {
    fun getById(id: String): Flow<Resource<Course>>{
        return courseRepository.getById(id)
    }
    fun getAll(): Flow<Resource<List<Course>>> {
        return courseRepository.getAll()
    }
}