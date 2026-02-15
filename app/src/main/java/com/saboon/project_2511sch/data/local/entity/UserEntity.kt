package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "app_version_at_creation") val appVersionAtCreation: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: Int,
    @ColumnInfo(name = "content_hash") val contentHash: String,
    @ColumnInfo(name = "server_version") val serverVersion: Int,

    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "last_login_at") val lastLoginAt: Long,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "academic_level") val academicLevel: String,
    @ColumnInfo(name = "organisation") val organisation: String,
    @ColumnInfo(name = "about_me") val aboutMe: String,
)