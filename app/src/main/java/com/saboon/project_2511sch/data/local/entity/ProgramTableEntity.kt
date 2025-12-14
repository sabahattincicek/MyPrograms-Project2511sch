package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.ModelColors

@Entity(tableName = "program_tables")
data class ProgramTableEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name ="created_by_user_id")val createdByUserId: String?,
    @ColumnInfo(name ="updated_by_user_id")val updatedByUserId: String?,

    @ColumnInfo(name ="created_at")val createdAt: Long,
    @ColumnInfo(name ="updated_at")val updatedAt: Long,
    @ColumnInfo(name ="is_deleted")val isDeleted: Boolean,
    @ColumnInfo(name ="row_version")val rowVersion: Int,

    @ColumnInfo(name ="title")val title: String?,
    @ColumnInfo(name ="description")val description: String?,
    @ColumnInfo(name ="color")val color: String?,

    @ColumnInfo(name ="is_active")val isActive: Boolean,
    @ColumnInfo(name ="is_shared")val isShared: Boolean
)