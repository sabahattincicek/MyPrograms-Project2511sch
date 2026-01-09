package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class UpdateScheduleUseCase @Inject constructor(
    private val scheduleRepository: IScheduleRepository
) {
    suspend operator fun invoke(task: Task): Resource<Task>{
        val updatedSchedule = task.copy(
            updatedAt = System.currentTimeMillis(),
            rowVersion = task.rowVersion + 1
        )
        return scheduleRepository.updateSchedule(updatedSchedule)
    }
}