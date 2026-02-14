package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.domain.model.Course

fun CourseEntity.toDomain(): Course {
    return Course(
        id = id,
        createdBy = createdBy,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        appVersionAtCreation = appVersionAtCreation,
        programTableId = programTableId,
        title = title,
        description = description,
        people = people,
        color = color,
        absence = absence
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        createdBy = createdBy,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        isActive = isActive,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        appVersionAtCreation = appVersionAtCreation,
        programTableId = programTableId,
        title = title,
        description = description,
        people = people,
        color = color,
        absence = absence
    )
}