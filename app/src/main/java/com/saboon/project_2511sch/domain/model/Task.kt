package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Task: BaseModel, Parcelable {

    @Parcelize
    data class Lesson(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val appVersionAtCreation: String,

        val programTableId: String,
        val courseId: String,

        val title: String,
        val description: String,
        val date: Long,
        val recurrenceRule: String,
        val timeStart: Long,
        val timeEnd: Long,
        val remindBefore: Int,
        val place: String,

    ): Parcelable, Task()

    @Parcelize
    data class Exam(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val appVersionAtCreation: String,

        val programTableId: String,
        val courseId: String,

        val title: String,
        val description: String,
        val date: Long,
        val timeStart: Long,
        val timeEnd: Long,
        val remindBefore: Int,
        val place: String,
        val targetScore: Int?,
        val achievedScore: Int?,

    ): Parcelable, Task()

    @Parcelize
    data class Homework(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val appVersionAtCreation: String,

        val programTableId: String,
        val courseId: String,

        val title: String,
        val description: String,
        val dueDate: Long,
        val dueTime: Long,
        val remindBefore: Int,
    ): Parcelable, Task()
}