package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.domain.model.Task

fun TaskHomeworkEntity.toDomain(): Task.Homework{
    return Task.Homework(
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
        courseId = courseId,
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime,
        remindBefore = remindBefore,
    )
}

fun Task.Homework.toEntity(): TaskHomeworkEntity{
    return TaskHomeworkEntity(
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
        courseId = courseId,
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime,
        remindBefore = remindBefore,
    )
}