package com.saboon.project_2511sch.domain.usecase.settings

import android.content.Context
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val sFileRepository: ISFileRepository,

) {

}