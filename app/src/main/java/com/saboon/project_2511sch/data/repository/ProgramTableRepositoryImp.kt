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
    override suspend fun insert(programTable: ProgramTable): Resource<ProgramTable> {
        try{
            programTableDao.insert(programTable.toEntity())
            return Resource.Success(programTable)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun update(programTable: ProgramTable): Resource<ProgramTable> {
        try {
            programTableDao.update(programTable.toEntity())
            return Resource.Success(programTable)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
    override suspend fun delete(programTable: ProgramTable): Resource<ProgramTable> {
        try {
            programTableDao.delete(programTable.toEntity())
            return Resource.Success(programTable)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun activationById(id: String, isActive: Boolean): Resource<Unit> {
        try {
            programTableDao.activationById(id, isActive)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getById(id: String): Flow<Resource<ProgramTable>> {
        return programTableDao.getById(id)
            .map<ProgramTableEntity, Resource<ProgramTable>> { entity ->
                Resource.Success(entity.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getAll(): Flow<Resource<List<ProgramTable>>> {
        return programTableDao.getAll()
            .map<List<ProgramTableEntity>, Resource<List<ProgramTable>>>{ programTableEntities ->
                Resource.Success(programTableEntities.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getAllActive(): Flow<Resource<List<ProgramTable>>>{
        return programTableDao.getAllActive()
            .map<List<ProgramTableEntity>, Resource<List<ProgramTable>>>{ entityList ->
                Resource.Success(entityList.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override suspend fun getAllCount(): Resource<Int> {
        try {
            val count = programTableDao.getAllCount()
            return Resource.Success(count)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    override suspend fun getAllActiveCount(): Resource<Int> {
        try {
            val count = programTableDao.getAllActiveCount()
            return Resource.Success(count)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }
}
