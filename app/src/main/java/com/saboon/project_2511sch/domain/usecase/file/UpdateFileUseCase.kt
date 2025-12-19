package com.saboon.project_2511sch.domain.usecase.file

import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class UpdateFileUseCase @Inject constructor(
    private val fileRepository: IFileRepository
) {
    suspend operator fun invoke(file: File): Resource<File>{
        val updatedFile = file.copy(
            rowVersion = file.rowVersion + 1,
            updatedAt = System.currentTimeMillis()
        )
        return fileRepository.updateFile(updatedFile)
    }
}