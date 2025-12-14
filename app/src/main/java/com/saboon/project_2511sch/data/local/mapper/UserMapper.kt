package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        authProviderId = this.authProviderId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        rowVersion = this.rowVersion,
        isActive = this.isActive,
        email = this.email,
        userName = this.userName,
        firstName = this.firstName,
        secondName = this.secondName,
        photoUrl = this.photoUrl,
        userRole = this.userRole,
        academicLevel = this.academicLevel,
        organization = this.organization,
        lastLoginAt = this.lastLoginAt,
        lastLoginIp = this.lastLoginIp,
        isVerified = this.isVerified
    )
}
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        authProviderId = this.authProviderId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isDeleted = this.isDeleted,
        rowVersion = this.rowVersion,
        isActive = this.isActive,
        email = this.email,
        userName = this.userName,
        firstName = this.firstName,
        secondName = this.secondName,
        photoUrl = this.photoUrl,
        userRole = this.userRole,
        academicLevel = this.academicLevel,
        organization = this.organization,
        lastLoginAt = this.lastLoginAt,
        lastLoginIp = this.lastLoginIp,
        isVerified = this.isVerified
    )
}