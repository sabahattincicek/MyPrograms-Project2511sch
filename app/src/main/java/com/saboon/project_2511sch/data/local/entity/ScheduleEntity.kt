package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saboon.project_2511sch.domain.model.Schedule

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "program_table_id") val programTableId: String,
    @ColumnInfo(name = "created_by_user_id") val createdByUserId: String?,
    @ColumnInfo(name = "updated_by_user_id") val updatedByUserId: String?,

    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "row_version") val rowVersion: Int,

    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "place") val place: String?,
    @ColumnInfo(name = "remind_before") val remindBefore: Int,
    @ColumnInfo(name = "recurrence_rule") val recurrenceRule: String
)