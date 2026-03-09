package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.TagDao
import com.saboon.project_2511sch.data.local.entity.TagEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class TagRepositoryImp @Inject constructor(
    private val tagDao: TagDao
): ITagRepository {
    override suspend fun insert(tag: Tag): Resource<Tag> {
        try{
            tagDao.insert(tag.toEntity())
            return Resource.Success(tag)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun update(tag: Tag): Resource<Tag> {
        try {
            tagDao.update(tag.toEntity())
            return Resource.Success(tag)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
    override suspend fun delete(tag: Tag): Resource<Tag> {
        try {
            tagDao.delete(tag.toEntity())
            return Resource.Success(tag)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
    override suspend fun activationById(
        id: String,
        isActive: Boolean
    ): Resource<Unit> {
        try {
            tagDao.activationById(id, isActive)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
    override fun getById(id: String): Flow<Resource<Tag>> {
        return tagDao.getById(id)
            .map<TagEntity, Resource<Tag>> { entity ->
                Resource.Success(entity.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getAll(): Flow<Resource<List<Tag>>> {
        return tagDao.getAll()
            .map<List<TagEntity>, Resource<List<Tag>>>{ programTableEntities ->
                Resource.Success(programTableEntities.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getAllActive(): Flow<Resource<List<Tag>>>{
        return tagDao.getAllActive()
            .map<List<TagEntity>, Resource<List<Tag>>>{ entityList ->
                Resource.Success(entityList.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

}
