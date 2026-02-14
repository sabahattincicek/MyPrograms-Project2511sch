package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.domain.model.ProgramTable

fun ProgramTableEntity.toDomain(): ProgramTable {
    return ProgramTable(
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
        title = title,
        description = description,
        color = color
    )
}

fun ProgramTable.toEntity(): ProgramTableEntity {
    return ProgramTableEntity(
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
        title = title,
        description = description,
        color = color
    )
}