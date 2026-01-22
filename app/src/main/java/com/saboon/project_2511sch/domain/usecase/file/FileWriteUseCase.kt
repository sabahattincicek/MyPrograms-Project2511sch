package com.saboon.project_2511sch.domain.usecase.file

import android.net.Uri
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class FileWriteUseCase @Inject constructor(
    private val fileRepository: IFileRepository
) {
    suspend fun insertFile(file: File, uri: Uri): Resource<File>{
        return fileRepository.insertFileFromUri(file, uri)
    }
    suspend fun insertLink(link: File): Resource<File>{
        return fileRepository.insertLinkFile(link)
    }
    suspend fun insertNote(note: File): Resource<File>{
        return fileRepository.insertNoteFile(note)
    }
    suspend fun update(file: File): Resource<File>{
        val updatedFile = file.copy(
            version = file.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        return fileRepository.updateFile(updatedFile)
    }
    suspend fun delete(file: File): Resource<File>{
        return fileRepository.deleteFile(file)
    }
}