package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class DeleteCourseUseCase @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val scheduleRepository: IScheduleRepository
) {
    suspend operator fun invoke(course: Course): Resource<Course>{
        val resultDeleteSchedule = scheduleRepository.deleteSchedulesByCourseId(course.id)
        if(resultDeleteSchedule is Resource.Error){
            return Resource.Error(resultDeleteSchedule.message?:"deleteCourseUseCase: Unknown error")
        }

        return courseRepository.deleteCourse(course)
    }
}