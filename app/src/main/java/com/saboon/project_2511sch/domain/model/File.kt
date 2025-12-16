package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class File(
    val id: String,
    val programTableId: String,
    val courseId: String,
    val createByUserId: String? = null,
    val updatedByUserId: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val version: Int = 1,

    val title: String?,
    val description: String?,
    val fileType: String,
    val filePath: String,
    val sizeInBytes: Long,
): Parcelable
