package com.saboon.project_2511sch.domain.usecase.file

import android.net.Uri
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class InsertNewFileUseCase @Inject constructor(
    private val fileRepository: IFileRepository
) {
    suspend operator fun invoke(file: File, uri: Uri): Resource<File>{
        return fileRepository.insertFile(file, uri)
    }
}