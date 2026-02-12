package com.saboon.project_2511sch.domain.usecase.sfile

import android.net.Uri
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class SFileWriteUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository
) {
    suspend fun insert(sFile: SFile, uri: Uri): Resource<SFile>{
        return sFileRepository.insert(sFile, uri)
    }
    suspend fun update(sFile: SFile): Resource<SFile>{
        return sFileRepository.update(sFile)
    }
    suspend fun delete(sFile: SFile): Resource<SFile>{
        return sFileRepository.delete(sFile)
    }
}