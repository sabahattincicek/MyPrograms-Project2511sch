package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IProgramTableRepository {
    suspend fun insert(programTable: ProgramTable) : Resource<ProgramTable>
    suspend fun delete(programTable: ProgramTable) : Resource<ProgramTable>
    suspend fun update(programTable: ProgramTable) : Resource<Unit>
    fun getAll(): Flow<Resource<List<ProgramTable>>>
    fun getAllActive(): Flow<Resource<List<ProgramTable>>>
    suspend fun getAllCount(): Resource<Int>
    suspend fun getAllActiveCount(): Resource<Int>
}