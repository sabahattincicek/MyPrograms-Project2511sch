package com.saboon.project_2511sch.domain.usecase.sfile

import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.presentation.sfile.DisplayItemSFile
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SFileReadUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository
){

}