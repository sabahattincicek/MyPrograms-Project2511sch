package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_exams",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProgramTableEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_table_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskExamEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "version") val version: Int,

    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "program_table_id") val programTableId: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,

    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "time_start") val timeStart: Long,
    @ColumnInfo(name = "time_end") val timeEnd: Long,
    @ColumnInfo(name = "remind_before") val remindBefore: Int,
    @ColumnInfo(name = "exam_type") val examType: String?,
    @ColumnInfo(name = "place") val place: String?,
    @ColumnInfo(name = "target_score") val targetScore: Int?,
    @ColumnInfo(name = "achieved_score") val achievedScore: Int?
)
