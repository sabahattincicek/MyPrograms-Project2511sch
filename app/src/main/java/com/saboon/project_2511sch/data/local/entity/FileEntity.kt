package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "files",
//    // Foreign Key ile Course'a bağlıyoruz.
//    // Bir Course silindiğinde, ona ait tüm dosyaların da silinmesini sağlıyoruz (onDelete = CASCADE).
//    foreignKeys = [
//        ForeignKey(
//            entity = CourseEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["course_id"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
)
data class FileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "program_table_id") val programTableId: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "create_by_user_id") val createdByUserId: String? = null,
    @ColumnInfo(name = "updated_by_user_id") val updatedByUserId: String? = null,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "row_version") val rowVersion: Int = 1,

    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "file_type") val fileType: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "size_in_bytes") val sizeInBytes: Long,
)
