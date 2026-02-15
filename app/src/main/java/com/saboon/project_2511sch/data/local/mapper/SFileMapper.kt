package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.SFileEntity
import com.saboon.project_2511sch.domain.model.SFile

fun SFileEntity.toDomain(): SFile {
    return SFile(
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
        title = title,
        description = description,
        programTableId = programTableId,
        courseId = courseId,
        taskId = taskId,
        filePath = filePath
    )
}

fun SFile.toEntity(): SFileEntity {
    return SFileEntity(
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
        title = title,
        description = description,
        programTableId = programTableId,
        courseId = courseId,
        taskId = taskId,
        filePath = filePath
    )
}