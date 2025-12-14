package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import com.saboon.project_2511sch.domain.model.ProgramTable

fun ProgramTableEntity.toDomain(): ProgramTable {
    return ProgramTable(
        id = id,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        color = color,
        isActive = isActive,
        isShared = isShared
    )
}

fun ProgramTable.toEntity(): ProgramTableEntity {
    return ProgramTableEntity(
        id = id,
        createdByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        color = color,
        isActive = isActive,
        isShared = isShared
    )
}