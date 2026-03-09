package com.saboon.project_2511sch.presentation.profile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.toFormattedString
import kotlinx.serialization.Serializable

@Serializable
data class DataTransferPackage( // for export and import operations
    val tags: List<Tag>,
    val courses: List<Course>,
    val tasks: List<Task>,
    val sFiles: List<SFile>,
    val exportDate: String = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHddss")
)