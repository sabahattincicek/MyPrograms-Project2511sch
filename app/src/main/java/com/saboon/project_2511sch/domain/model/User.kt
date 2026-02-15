package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
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

    val userName: String,
    val email: String,
    val photoUrl: String,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val fullName: String,
    val role: String,
    val academicLevel: String,
    val organisation: String,
    val aboutMe: String,
): BaseModel, Parcelable
