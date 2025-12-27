package com.saboon.project_2511sch.domain.usecase.schedule

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSchedulesByCourseIdUseCase @Inject constructor(
    private val scheduleRepository: IScheduleRepository
) {
operator fun invoke(id: String): Flow<Resource<List<Task>>> {
        return scheduleRepository.getSchedulesByCourseId(id)
    }
}