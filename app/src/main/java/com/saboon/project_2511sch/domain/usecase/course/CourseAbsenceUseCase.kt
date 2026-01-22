package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class CourseAbsenceUseCase @Inject constructor(
    private val courseRepository: ICourseRepository
) {
    suspend fun increment(course: Course): Resource<Course>{
        val incrementedCourse = course.copy(
            absence = course.absence + 1
        )
        return courseRepository.updateCourse(incrementedCourse)
    }
    suspend fun decrement(course: Course): Resource<Course>{
        val decrementedCourse = course.copy(
            absence = course.absence - 1
        )
        return courseRepository.updateCourse(decrementedCourse)
    }
}