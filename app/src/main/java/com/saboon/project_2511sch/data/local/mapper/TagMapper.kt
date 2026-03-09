package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.TagEntity
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.util.ModelColor

fun TagEntity.toDomain(): Tag {
    return Tag(
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
        color = ModelColor(color)
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
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
        color = color.colorHex
    )
}