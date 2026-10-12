package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.domain.model.User

abstract class TestBaseRepository {
    val baseUserEntity = UserEntity(
        id = "test-user",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        version = 1,
        isActive = true,
        isDeleted = false,
        deletedAt = 0L,
        syncStatus = 0,
        contentHash = "",
        serverVersion = 1,
        userName = "",
        email = "",
        photoUrl = "",
        lastLoginAt = 0L,
        fullName = "",
        role = "",
        academicLevel = "",
        institution = "",
        aboutMe = ""
    )
    val baseUser = User(
        id = "test-user",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        version = 1,
        isActive = true,
        isDeleted = false,
        deletedAt = 0L,
        syncStatus = 1,
        contentHash = "",
        serverVersion = 1,
        userName = "",
        email = "",
        photoUrl = "",
        lastLoginAt = 0L,
        fullName = "",
        role = "",
        academicLevel = "",
        institution = "",
        aboutMe = ""
    )
}