package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class SetAllProgramTablesInactiveUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    suspend operator fun invoke(): Resource<Unit>{
        return programTableRepository.setInactiveAllProgramTable()
    }
}