package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveProgramTableUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    operator fun invoke(): Flow<Resource<ProgramTable>> {
        return programTableRepository.getActiveProgramTable()
    }
}