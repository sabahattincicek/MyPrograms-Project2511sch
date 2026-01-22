package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity
import com.saboon.project_2511sch.domain.model.Task

fun TaskLessonEntity.toDomain(): Task.Lesson {
    return Task.Lesson(
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
        recurrenceRule = recurrenceRule,
        timeStart = timeStart,
        timeEnd = timeEnd,
        remindBefore = remindBefore,
        place = place
    )
}

fun Task.Lesson.toEntity(): TaskLessonEntity{
    return TaskLessonEntity(
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
        recurrenceRule = recurrenceRule,
        timeStart = timeStart,
        timeEnd = timeEnd,
        remindBefore = remindBefore,
        place = place
    )
}