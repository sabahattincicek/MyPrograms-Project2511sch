package com.saboon.project_2511sch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, //when first start the user created and set the userId
    @ColumnInfo(name = "auth_provider_id") val authProviderId: String?, //when user set the remote and sync this field creates

    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "row_version") val rowVersion: Int,
    @ColumnInfo(name = "is_active") val isActive: Boolean,

    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "second_name") val secondName: String?,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "user_role") val userRole: String?, //student, teacher, ...
    @ColumnInfo(name = "academic_level") val academicLevel: String?, //bachelor 2. level, lisans 1. sinif
    @ColumnInfo(name = "organization") val organization: String?,

    @ColumnInfo(name = "last_login_at") val lastLoginAt: Long?,
    @ColumnInfo(name = "last_login_ip") val lastLoginIp: String?,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean
)
