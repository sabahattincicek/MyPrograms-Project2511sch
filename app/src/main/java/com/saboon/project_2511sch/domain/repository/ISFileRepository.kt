package com.saboon.project_2511sch.domain.repository

import android.net.Uri
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface ISFileRepository {
    suspend fun insert(sFile: SFile, uri: Uri): Resource<SFile> //for file selector in android
    suspend fun insert(sFile: SFile): Resource<SFile> //for import data
    suspend fun update(sFile: SFile): Resource<SFile>
    suspend fun delete(sFile: SFile): Resource<SFile>
    fun getAll(): Flow<Resource<List<SFile>>>

}