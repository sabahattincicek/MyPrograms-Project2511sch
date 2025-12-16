package com.saboon.project_2511sch.data.local.mapper

import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.domain.model.File

fun FileEntity.toDomain(): File {
    return File(
        id = id,
        programTableId = programTableId,
        courseId = courseId,
        createByUserId = createdByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
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
        programTableId = programTableId,
        courseId = courseId,
        createdByUserId = createByUserId,
        updatedByUserId = updatedByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        rowVersion = rowVersion,
        title = title,
        description = description,
        fileType = fileType,
        filePath = filePath,
        sizeInBytes = sizeInBytes
    )
}