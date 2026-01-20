package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IProgramTableRepository {
    suspend fun insertProgramTable(programTable: ProgramTable) : Resource<ProgramTable>
    suspend fun deleteProgramTable(programTable: ProgramTable) : Resource<ProgramTable>
    suspend fun updateProgramTable(programTable: ProgramTable) : Resource<Unit>
    fun getAllProgramTableList(): Flow<Resource<List<ProgramTable>>>
    fun getActiveProgramTableList(): Flow<Resource<List<ProgramTable>>>
    suspend fun setProgramTableActive(programTable: ProgramTable): Resource<Unit>
    suspend fun setProgramTableInActive(programTable: ProgramTable): Resource<Unit>

}