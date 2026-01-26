package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class CourseWriteUseCase @Inject constructor(
    private val courseRepository: ICourseRepository
) {
    suspend fun insert(course: Course): Resource<Course>{
        return courseRepository.insert(course)
    }
    suspend fun update(course: Course): Resource<Course>{
        val updatedCourse = course.copy(
            version = course.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        return courseRepository.update(updatedCourse)
    }
    suspend fun delete(course: Course): Resource<Course>{
        return courseRepository.delete(course)
    }
    suspend fun activationById(id: String, isActive: Boolean): Resource<Unit>{
        return courseRepository.activationById(id, isActive)
    }
}