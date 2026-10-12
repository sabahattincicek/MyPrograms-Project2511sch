package com.saboon.project_2511sch.domain.model

import com.saboon.project_2511sch.util.toFormattedString
import kotlinx.serialization.Serializable

/**
 * The complete user data set as it is written to and read from a backup file.
 *
 * ExportDataUseCase serializes one instance to `export.json` inside the backup ZIP and
 * ImportDataUseCase reads it back. The field names are part of the backup file format, so
 * renaming them breaks backups created by earlier versions.
 *
 * @property tags All tags.
 * @property courses All courses.
 * @property tasks All lessons, exams and homeworks.
 * @property sFiles Metadata of all attached files; the files themselves are stored next to the JSON.
 * @property exportDate When the backup was created, as text.
 */
@Serializable
data class DataTransferPackage(
    val tags: List<Tag>,
    val courses: List<Course>,
    val tasks: List<Task>,
    val sFiles: List<SFile>,
    val exportDate: String = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHddss")
)
