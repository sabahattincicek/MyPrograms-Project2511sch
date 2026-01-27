package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.domain.model.File

fun FileEntity.toDomain(): File {
    return File(
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
        taskId = taskId,
        title = title,
        description = description,
        fileType = fileType,
        filePath = filePath,
        sizeInBytes = sizeInBytes
    )
}

fun File.toEntity(): FileEntity {
    return FileEntity(
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
        taskId = taskId,
        title = title,
        description = description,
        fileType = fileType,
        filePath = filePath,
        sizeInBytes = sizeInBytes
    )
}