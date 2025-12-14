package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.ScheduleEntity
import com.saboon.project_2511sch.domain.model.Schedule

fun ScheduleEntity.toDomain(): Schedule {
    return Schedule(
        id = id,
        courseId = courseId,
        programTableId = programTableId,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        place = place,
        remindBefore = remindBefore,
        recurrenceRule = recurrenceRule
    )
}

fun Schedule.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        courseId = courseId,
        programTableId = programTableId,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        place = place,
        remindBefore = remindBefore,
        recurrenceRule = recurrenceRule
    )
}