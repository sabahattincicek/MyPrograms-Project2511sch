package com.saboon.project_2511sch.domain.usecase.schedule

import com.saboon.project_2511sch.domain.model.Schedule
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class InsertNewScheduleUseCase @Inject constructor(
    private val scheduleRepository: IScheduleRepository
) {
    suspend operator fun invoke(schedule: Schedule): Resource<Schedule>{
        return scheduleRepository.insertSchedule(schedule)
    }
}