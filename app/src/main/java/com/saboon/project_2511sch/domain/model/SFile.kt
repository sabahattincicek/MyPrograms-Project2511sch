package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SFile(
    override val id: String,
    override val createdBy: String,
    override val updatedBy: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val version: Int = 0,
    override val isActive: Boolean = true,
    override val isDeleted: Boolean = false,
    override val deletedAt: Long = 0L,
    override val appVersionAtCreation: String,
    override val title: String,
    override val description: String,

    val programTableId: String?,
    val courseId: String?,
    val taskId: String?,

    val filePath: String,
): BaseModel, Parcelable
