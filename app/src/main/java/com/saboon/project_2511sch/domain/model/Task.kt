package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: String,
    val courseId: String,
    val programTableId: String,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val rowVersion: Int = 1,

    val title: String?,
    val description: String?,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val place: String?,
    val remindBefore: Int,
    val recurrenceRule: String
) : Parcelable