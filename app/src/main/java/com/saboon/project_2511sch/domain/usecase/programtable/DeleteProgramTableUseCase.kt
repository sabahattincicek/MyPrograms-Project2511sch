package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteProgramTableUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
) {
    suspend operator fun invoke(programTable: ProgramTable): Resource<ProgramTable>{
        return programTableRepository.deleteProgramTable(programTable)
    }
}