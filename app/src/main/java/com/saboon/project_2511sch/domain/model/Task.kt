package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Task: BaseModel, BaseTask, Parcelable {

    @Parcelize
    data class Lesson(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,

        override val courseId: String,
        override val programTableId: String,
        override val title: String?,
        override val description: String?,
        override val type: TaskType,

        val date: Long,
        val dateRangeStart: Long,
        val dateRangeEnd: Long,
        val recurrenceRule: String,
        val timeStart: Long,
        val timeEnd: Long,
        val remindBefore: Int,
        val place: String?,
    ): Parcelable, Task()

    @Parcelize
    data class Exam(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,

        override val courseId: String,
        override val programTableId: String,
        override val title: String?,
        override val description: String?,
        override val type: TaskType,

        val date: Long,
        val timeStart: Long,
        val timeEnd: Long,
        val examType: ExamType,
        val remindBefore: Int = 0,
        val place: String?,
        val targetScore: Int,
        val achievedScore: Int
    ): Parcelable, Task()

    @Parcelize
    data class Homework(
        override val id: String,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val version: Int = 0,

        override val courseId: String,
        override val programTableId: String,
        override val title: String?,
        override val description: String?,
        override val type: TaskType,

        val dueDate: Long,
        val remindBefore: Int = 0,
        val link: String?,
        val submissionType: SubmissionType,
    ): Parcelable, Task()
}

enum class ExamType {
    //dont forget edit the string array
    OTHER, MIDTERM, FINAL, QUIZ
}
enum class SubmissionType {
    //dont forget edit the string array
    OTHER, ONLINE, PHYSICAL
}