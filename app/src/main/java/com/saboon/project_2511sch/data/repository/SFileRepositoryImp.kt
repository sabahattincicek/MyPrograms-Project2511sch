package com.saboon.project_2511sch.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.saboon.project_2511sch.data.local.dao.SFileDao
import com.saboon.project_2511sch.data.local.entity.SFileEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SFileRepositoryImp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sFileDao: SFileDao
): ISFileRepository {
    override suspend fun insert(sFile: SFile, uri: Uri): Resource<SFile> {
        val contentResolver = context.contentResolver

        var originalFileName = "unknown_file"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()){
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) originalFileName = cursor.getString(nameIndex)
            }
        }

        try {
            // 1. Fiziksel dosyayı uygulamanın özel alanına kopyala
            val inputStream = contentResolver.openInputStream(uri)
            val newPhysicalFileName = "${System.currentTimeMillis()}_${originalFileName}"
            val newLocalFile = File(context.filesDir, newPhysicalFileName)
            val outputStream = FileOutputStream(newLocalFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val sFileToSave = sFile.copy(
                id = IdGenerator.generateFileId(originalFileName),
                title = originalFileName,
                filePath = newLocalFile.absolutePath,
            )

            sFileDao.insert(sFileToSave.toEntity())
            return Resource.Success(sFile)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun update(sFile: SFile): Resource<SFile> {
        try {
            sFileDao.update(sFile.toEntity())
            return Resource.Success(sFile)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun delete(sFile: SFile): Resource<SFile> {
        try {
            val fileToDelete = File(sFile.filePath)
            if (fileToDelete.exists() && !fileToDelete.delete()){
                return Resource.Error("Failed to delete physical file at ${sFile.filePath}")
            }
            sFileDao.delete(sFile.toEntity())
            return Resource.Success(sFile)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
    override fun getAll(): Flow<Resource<List<SFile>>>{
        return sFileDao.getAll()
            .map<List<SFileEntity>, Resource<List<SFile>>> { sFileEntities ->
                Resource.Success(sFileEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }
}