package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskLessonEntity
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.RecurrenceRule

fun TaskLessonEntity.toDomain(): Task.Lesson {
    return Task.Lesson(
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
        date = date,
        recurrenceRule = RecurrenceRule.fromRuleString(recurrenceRuleString),
        timeStart = timeStart,
        timeEnd = timeEnd,
        absence = absence.split(",")
            .filter { it.isNotBlank() }
            .map { it.toLong() },
        remindBefore = remindBefore,
        place = place
    )
}

fun Task.Lesson.toEntity(): TaskLessonEntity{
    return TaskLessonEntity(
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
        date = date,
        recurrenceRuleString = recurrenceRule.toRuleString(),
        timeStart = timeStart,
        timeEnd = timeEnd,
        absence = absence.joinToString(","),
        remindBefore = remindBefore,
        place = place
    )
}