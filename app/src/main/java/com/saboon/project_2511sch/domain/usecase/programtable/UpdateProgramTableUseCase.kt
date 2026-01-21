package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class UpdateProgramTableUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    suspend operator fun invoke(programTable: ProgramTable): Resource<Unit> {
        val updatedProgramTable = programTable.copy(
            version = programTable.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        return programTableRepository.updateProgramTable(updatedProgramTable)
    }
}