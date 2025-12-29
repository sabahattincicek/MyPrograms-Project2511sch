package com.saboon.project_2511sch.domain.repository

import android.net.Uri
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IFileRepository {

    suspend fun insertFileFromUri(file: File, uri: Uri): Resource<File>

    suspend fun deleteFile(file: File): Resource<File>

    suspend fun updateFile(file: File): Resource<File>

    suspend fun insertNoteFile(note: File): Resource<File>

    suspend fun insertLinkFile(link: File): Resource<File>

    fun getFilesByCourseId(id: String): Flow<Resource<List<File>>>

    fun getFilesByProgramTableId(id: String): Flow<Resource<List<File>>>

    fun getAllFiles(): Flow<Resource<List<File>>>
}