package com.saboon.project_2511sch.domain.usecase.schedule

import com.saboon.project_2511sch.domain.model.Schedule
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    private val scheduleRepository: IScheduleRepository
) {
    suspend operator fun invoke(schedule: Schedule): Resource<Schedule>{
        return scheduleRepository.deleteSchedule(schedule)
    }
}