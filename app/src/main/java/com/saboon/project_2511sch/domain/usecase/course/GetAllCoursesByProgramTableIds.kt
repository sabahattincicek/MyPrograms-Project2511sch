package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCoursesByProgramTableIds @Inject constructor(
    private val courseRepository: ICourseRepository
) {
    operator fun invoke(ids: List<String>): Flow<Resource<List<Course>>>{
        return courseRepository.getAllCoursesByProgramTableIds(ids)
    }
}