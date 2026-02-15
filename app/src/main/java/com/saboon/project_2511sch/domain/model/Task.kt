package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import com.saboon.project_2511sch.util.RecurrenceRule
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Task: BaseModel, Parcelable {

    abstract val programTableId: String
    abstract val courseId: String
    abstract val remindBefore: Int
    abstract val title: String
    abstract val description: String

    @Parcelize
    data class Lesson(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val createdBy: String,
        override val appVersionAtCreation: String,
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 1,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val syncStatus: Int = 0,
        override val contentHash: String = "",
        override val serverVersion: Int = 1,

        override val programTableId: String,
        override val courseId: String,

        override val remindBefore: Int,
        override val title: String,
        override val description: String,

        val date: Long,
        val recurrenceRule: RecurrenceRule,
        val timeStart: Long,
        val timeEnd: Long,
        val place: String,

    ): Parcelable, Task()

    @Parcelize
    data class Exam(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val createdBy: String,
        override val appVersionAtCreation: String,
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 1,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val syncStatus: Int = 0,
        override val contentHash: String = "",
        override val serverVersion: Int = 1,

        override val programTableId: String,
        override val courseId: String,

        override val remindBefore: Int,
        override val title: String,
        override val description: String,

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
        override val createdBy: String,
        override val appVersionAtCreation: String,
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 1,
        override val isActive: Boolean = true,
        override val isDeleted: Boolean = false,
        override val deletedAt: Long = 0L,
        override val syncStatus: Int = 0,
        override val contentHash: String = "",
        override val serverVersion: Int = 1,

        override val programTableId: String,
        override val courseId: String,

        override val remindBefore: Int,
        override val title: String,
        override val description: String,

        val dueDate: Long,
        val dueTime: Long,
    ): Parcelable, Task()
}