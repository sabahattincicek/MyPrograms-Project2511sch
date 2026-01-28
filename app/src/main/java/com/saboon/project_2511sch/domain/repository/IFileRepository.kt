package com.saboon.project_2511sch.domain.repository

import android.net.Uri
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IFileRepository {

    suspend fun insertFile(file: File, uri: Uri): Resource<File>
    suspend fun insertNote(note: File): Resource<File>
    suspend fun insertLink(link: File): Resource<File>
    suspend fun delete(file: File): Resource<File>
    suspend fun update(file: File): Resource<File>

    fun getAllByTaskId(id: String): Flow<Resource<List<File>>>
    fun getFilesByCourseId(id: String): Flow<Resource<List<File>>>
    fun getFilesByProgramTableId(id: String): Flow<Resource<List<File>>>
    fun getAllFiles(): Flow<Resource<List<File>>>
}






