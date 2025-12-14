package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.domain.model.Course

fun CourseEntity.toDomain(): Course {
    return Course(
        id = id,
        programTableId = programTableId,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        people = people,
        color = color,
        absence = absence,
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        programTableId = programTableId,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        people = people,
        color = color,
        absence = absence,
    )
}