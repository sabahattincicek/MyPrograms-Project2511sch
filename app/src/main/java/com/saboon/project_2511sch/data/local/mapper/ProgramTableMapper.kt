package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.ModelColor

fun ProgramTableEntity.toDomain(): ProgramTable {
    return ProgramTable(
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

fun ProgramTable.toEntity(): ProgramTableEntity {
    return ProgramTableEntity(
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