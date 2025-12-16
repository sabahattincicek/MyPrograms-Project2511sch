package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class FileRepositoryImp @Inject constructor(
    private val fileDao: FileDao,
): IFileRepository {
    override suspend fun insertFile(file: File): Resource<File> {
       try {
            fileDao.insert(file)
           return Resource.Success(file)
        }catch (e: Exception){
           return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
}