package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Task: BaseModel, Parcelable {

    abstract val programTableId: String
    abstract val courseId: String
    abstract val remindBefore: Int

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
        override val title: String,
        override val description: String,

        override val programTableId: String,
        override val courseId: String,
        override val remindBefore: Int,

        val date: Long,
        val recurrenceRule: String,
        val timeStart: Long,
        val timeEnd: Long,
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
        override val title: String,
        override val description: String,

        override val programTableId: String,
        override val courseId: String,
        override val remindBefore: Int,

        val date: Long,
        val timeStart: Long,
        val timeEnd: Long,
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
        override val title: String,
        override val description: String,

        override val programTableId: String,
        override val courseId: String,
        override val remindBefore: Int,

        val dueDate: Long,
        val dueTime: Long,
    ): Parcelable, Task()
}