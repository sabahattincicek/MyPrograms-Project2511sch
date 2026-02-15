package com.saboon.project_2511sch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseBackup(
    val programTables: List<ProgramTable>,
    val courses: List<Course>,
    val tasks: List<Task>,
    val sFiles: List<SFile>,
    val exportDate: Long = System.currentTimeMillis()
)
