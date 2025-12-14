package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class InsertNewProgramTableUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
){
    suspend operator fun invoke(programTable: ProgramTable): Resource<ProgramTable>{
        val result = programTableRepository.setInactiveAllProgramTable()
        if (result is Resource.Error) return Resource.Error(result.message?:"setAllProgramTableToInactive: Unknown Error.")
        return programTableRepository.insertProgramTable(programTable)
    }
}