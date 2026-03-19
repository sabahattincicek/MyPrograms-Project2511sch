package com.saboon.project_2511sch.domain.usecase.tag

import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class TagWriteUseCase @Inject constructor(
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository
) {
    suspend fun insert(tag: Tag): Resource<Tag>{
        return tagRepository.insert(tag)
    }

    suspend fun update(tag: Tag): Resource<Tag> {
        val updatedTag = tag.copy(
            version = tag.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        return tagRepository.update(updatedTag)
    }
    suspend fun delete(tag: Tag): Resource<Tag>{
        courseRepository.removeTagFromCourses(tag.id)
        return tagRepository.delete(tag)
    }
    suspend fun activationById(id: String, isActive: Boolean): Resource<Unit>{
        return tagRepository.activationById(id, isActive)
    }
}