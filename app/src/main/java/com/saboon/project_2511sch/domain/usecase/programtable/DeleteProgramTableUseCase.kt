package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteProgramTableUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val scheduleRepository: IScheduleRepository
) {
    suspend operator fun invoke(programTable: ProgramTable): Resource<ProgramTable>{
        val resultDeleteSchedules = scheduleRepository.deleteSchedulesByProgramTableId(programTable.id)
        if (resultDeleteSchedules is Resource.Error){
            return Resource.Error(resultDeleteSchedules.message?:"deleteProgramTableUseCase: Unknown Error")
        }
        val resultDeleteCourses = courseRepository.deleteCoursesByProgramTableId(programTable.id)
        if (resultDeleteCourses is Resource.Error){
            return Resource.Error(resultDeleteCourses.message?:"deleteProgramTableUseCase: Unknown Error")
        }
        return programTableRepository.deleteProgramTable(programTable)
    }
}