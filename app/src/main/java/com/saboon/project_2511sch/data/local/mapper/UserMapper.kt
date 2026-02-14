package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.domain.model.User

fun UserEntity.toDomain(): User{
    return User(
        id = id,
        createdAt = createdAt,
        createdBy = createdBy,
        appVersionAtCreation = appVersionAtCreation,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        syncStatus = syncStatus,
        contentHash = contentHash,
        serverVersion = serverVersion,
        userName = userName,
        email = email,
        photoUrl = photoUrl,
        lastLoginAt = lastLoginAt,
        fullName = fullName,
        role = role,
        academicLevel = academicLevel,
        organisation = organisation
    )
}
fun User.toEntity(): UserEntity{
    return UserEntity(
        id = id,
        createdAt = createdAt,
        createdBy = createdBy,
        appVersionAtCreation = appVersionAtCreation,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        syncStatus = syncStatus,
        contentHash = contentHash,
        serverVersion = serverVersion,
        userName = userName,
        email = email,
        photoUrl = photoUrl,
        lastLoginAt = lastLoginAt,
        fullName = fullName,
        role = role,
        academicLevel = academicLevel,
        organisation = organisation
    )
}