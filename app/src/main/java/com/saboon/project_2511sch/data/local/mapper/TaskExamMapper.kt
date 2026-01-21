package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskExamEntity
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.TaskType

fun TaskExamEntity.toDomain(): Task.Exam{
    return Task.Exam(
        id = id,
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
        date = date,
        timeStart = timeStart,
        timeEnd = timeEnd,
        remindBefore = remindBefore,
        place = place,
        targetScore = targetScore,
        achievedScore = achievedScore
    )
}