package com.saboon.project_2511sch.domain.usecase.file

import android.R
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFilesByCourseId @Inject constructor(
    private val fileRepository: IFileRepository
) {
    operator fun invoke(id: String): Flow<Resource<List<File>>>{
        return fileRepository.getFilesByCourseId(id)
    }
}