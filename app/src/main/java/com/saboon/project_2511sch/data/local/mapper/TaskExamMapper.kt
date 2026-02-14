package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.domain.model.Task

fun TaskExamEntity.toDomain(): Task.Exam{
    return Task.Exam(
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
        programTableId = programTableId,
        courseId = courseId,
        title = title,
        description = description,
        date = date,
        timeStart = timeStart,
        timeEnd = timeEnd,
        remindBefore = remindBefore,
        place = place,
        targetScore = targetScore,
        achievedScore = achievedScore
    )
}

fun Task.Exam.toEntity(): TaskExamEntity{
    return TaskExamEntity(
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
        programTableId = programTableId,
        courseId = courseId,
        title = title,
        description = description,
        date = date,
        timeStart = timeStart,
        timeEnd = timeEnd,
        remindBefore = remindBefore,
        place = place,
        targetScore = targetScore,
        achievedScore = achievedScore
    )
}