package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.util.ModelColor

fun CourseEntity.toDomain(): Course {
    return Course(
        id = id,
        createdAt = createdAt,
        createdBy = createdBy,
        appVersionAtCreation = appVersionAtCreation,
        updatedAt = updatedAt,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        syncStatus = syncStatus,
        contentHash = contentHash,
        serverVersion = serverVersion,
        programTableId = programTableId,
        title = title,
        description = description,
        people = people,
        color = ModelColor(color),
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        createdAt = createdAt,
        createdBy = createdBy,
        appVersionAtCreation = appVersionAtCreation,
        updatedAt = updatedAt,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        syncStatus = syncStatus,
        contentHash = contentHash,
        serverVersion = serverVersion,
        programTableId = programTableId,
        title = title,
        description = description,
        people = people,
        color = color.colorHex,
    )
}