package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.saboon.project_2511sch.domain.model.Course

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = ProgramTableEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_table_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CourseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "updated_by") val updatedBy: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long,
    @ColumnInfo(name = "app_version_at_creation") val appVersionAtCreation: String,

    @ColumnInfo(name = "program_table_id") val programTableId: String,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "people") val people: String,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "absence") val absence: Int,
)