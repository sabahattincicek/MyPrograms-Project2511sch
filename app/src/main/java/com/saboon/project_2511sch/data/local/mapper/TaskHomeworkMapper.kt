package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.domain.model.Task

fun TaskHomeworkEntity.toDomain(): Task.Homework{
    return Task.Homework(
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
        courseId = courseId,
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime,
        remindBefore = remindBefore,
    )
}