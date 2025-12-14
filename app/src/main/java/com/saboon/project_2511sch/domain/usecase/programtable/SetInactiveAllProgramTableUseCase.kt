package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import jakarta.inject.Inject

class SetProgramTableToInactiveUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    suspend operator fun invoke(): Resource<Unit>{
        return programTableRepository.setInactiveAllProgramTable()
    }
}