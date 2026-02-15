package com.saboon.project_2511sch.presentation.profile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.toFormattedString
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseBackup(
    val programTables: List<ProgramTable>,
    val courses: List<Course>,
    val tasks: List<Task>,
    val sFiles: List<SFile>,
    val exportDate: String = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHddss")
)