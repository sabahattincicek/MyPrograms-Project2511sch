package com.saboon.project_2511sch.domain.usecase.tag

import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagReadUseCase @Inject constructor(
    private val tagRepository: ITagRepository
) {
    fun getById(id: String): Flow<Resource<Tag>>{
        return tagRepository.getById(id)
    }
    fun getAll(): Flow<Resource<List<Tag>>> {
        return tagRepository.getAll()
    }
    fun getAllActive(): Flow<Resource<List<Tag>>> {
        return tagRepository.getAllActive()
    }
}