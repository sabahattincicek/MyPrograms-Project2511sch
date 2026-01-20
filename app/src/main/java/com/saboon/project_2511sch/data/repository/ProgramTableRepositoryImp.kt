package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.ProgramTableDao
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class ProgramTableRepositoryImp @Inject constructor(
    private val programTableDao: ProgramTableDao
): IProgramTableRepository {
    override suspend fun insertProgramTable(programTable: ProgramTable): Resource<ProgramTable> {
        try{
            programTableDao.insert(programTable.toEntity())
            return Resource.Success(programTable)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun updateProgramTable(programTable: ProgramTable): Resource<Unit> {
        try {
            programTableDao.update(programTable.toEntity())
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun deleteProgramTable(programTable: ProgramTable): Resource<ProgramTable> {
        try {
            programTableDao.delete(programTable.toEntity())
            return Resource.Success(programTable)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getAllProgramTableList(): Flow<Resource<List<ProgramTable>>> {
        return programTableDao.getAllProgramTables()
            .map<List<ProgramTableEntity>, Resource<List<ProgramTable>>>{ programTableEntities ->
                Resource.Success(programTableEntities.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getActiveProgramTableList(): Flow<Resource<List<ProgramTable>>>{
        return programTableDao.getActiveProgramTableList()
            .map<List<ProgramTableEntity>, Resource<List<ProgramTable>>>{ entityList ->
                Resource.Success(entityList.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override suspend fun setProgramTableActive(programTable: ProgramTable): Resource<Unit> {
        return try {
            programTableDao.setActiveById(programTable.id)
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    override suspend fun setProgramTableInActive(programTable: ProgramTable): Resource<Unit> {
        return try {
            programTableDao.setInActiveById(programTable.id)
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }


}
