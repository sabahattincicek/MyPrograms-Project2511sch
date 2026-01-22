package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProgramTableReadUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    fun getAll(): Flow<Resource<List<ProgramTable>>> {
        return programTableRepository.getAll()
    }
    fun getAllActive(): Flow<Resource<List<ProgramTable>>> {
        return programTableRepository.getAllActive()
    }
    suspend fun getAllCount(): Resource<Int>{
        return programTableRepository.getAllCount()
    }
    suspend fun getAllActiveCount(): Resource<Int>{
        return programTableRepository.getAllActiveCount()
    }
}