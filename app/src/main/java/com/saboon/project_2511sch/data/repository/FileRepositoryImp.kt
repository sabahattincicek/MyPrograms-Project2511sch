package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FileRepositoryImp @Inject constructor(
    private val fileDao: FileDao,
): IFileRepository {
    override suspend fun insertFile(file: File): Resource<File> {
       try {
            fileDao.insert(file.toEntity())
           return Resource.Success(file)
        }catch (e: Exception){
           return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getFilesByCourseId(id: String): Flow<Resource<List<File>>> {
        return fileDao.getFilesByCourseId(id)
            .map<List<FileEntity>, Resource<List<File>>> { entities ->
                Resource.Success( entities.map { it.toDomain() } )
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }
}