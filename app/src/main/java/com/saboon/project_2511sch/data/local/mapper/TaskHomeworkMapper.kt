package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TaskHomeworkEntity
import com.saboon.project_2511sch.domain.model.Task

fun TaskHomeworkEntity.toDomain(): Task.Homework{
    return Task.Homework(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        courseId = courseId,
        programTableId = programTableId,
        title = title,
        description = description,
        dueDate = dueDate,
        remindBefore = remindBefore,
        link = link,
        submissionType = submissionType
    )
}

fun Task.Homework.toEntity(): TaskHomeworkEntity{
    return TaskHomeworkEntity(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        courseId = courseId,
        programTableId = programTableId,
        title = title,
        description = description,
        dueDate = dueDate,
        remindBefore = remindBefore,
        link = link,
        submissionType = submissionType
    )
}