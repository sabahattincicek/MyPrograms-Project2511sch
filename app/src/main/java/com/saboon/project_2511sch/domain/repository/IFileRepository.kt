package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.Resource

interface IFileRepository {

    suspend fun insertFile(file: File): Resource<File>
}