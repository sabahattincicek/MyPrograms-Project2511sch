package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProgramTable(
    val id: String,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val rowVersion: Int = 1,

    val title: String?,
    val description: String?,
    val color: String?,

    val isActive: Boolean = true,
    val isShared: Boolean = false
): Parcelable
