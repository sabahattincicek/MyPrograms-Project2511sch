package com.saboon.project_2511sch.presentation.sfile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task

data class FilterFile(
    val programTable: ProgramTable? = null,
    val course: Course? = null,
    val task: Task? = null
)