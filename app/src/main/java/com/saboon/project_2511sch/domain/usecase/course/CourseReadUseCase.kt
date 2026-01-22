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
        return courseRepository.getCourseById(id)
    }
    fun getAllByProgramTableId(id: String): Flow<Resource<List<Course>>>{
        return courseRepository.getCoursesByProgramTableId(id)
    }
    fun getAllByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>>{
        return courseRepository.getAllCoursesByProgramTableIds(ids)
    }
    fun getAll(): Flow<Resource<List<Course>>> {
        return courseRepository.getAllCourses()
    }

}