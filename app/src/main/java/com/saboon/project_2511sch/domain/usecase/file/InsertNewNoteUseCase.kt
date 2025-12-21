package com.saboon.project_2511sch.domain.usecase.file

import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class InsertNewNoteUseCase @Inject constructor(
    private val fileRepository: IFileRepository
){
    suspend operator fun invoke(note: File): Resource<File>{
        return fileRepository.insertNoteFile(note)
    }
}