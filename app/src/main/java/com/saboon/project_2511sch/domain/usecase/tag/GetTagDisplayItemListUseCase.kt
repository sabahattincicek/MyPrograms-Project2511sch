package com.saboon.project_2511sch.domain.usecase.tag

import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.presentation.tag.DisplayItemTag
import javax.inject.Inject
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTagDisplayItemListUseCase @Inject constructor(
    private val tagRepository: ITagRepository
) {
    operator fun invoke(): Flow<Resource<List<DisplayItemTag>>> {
        return tagRepository.getAll().map { resource ->
            when(resource) {
                is Resource.Error -> {Resource.Error(resource.message ?: "An Unknown error occurred")}
                is Resource.Idle -> {Resource.Idle()}
                is Resource.Loading -> {Resource.Loading()}
                is Resource.Success -> {
                    val programTables = resource.data ?: emptyList()

                    if (programTables.isEmpty()) {
                        return@map Resource.Success(emptyList())
                    }

                    val displayList = mutableListOf<DisplayItemTag>()
                    displayList.addAll(programTables.map {
                        DisplayItemTag.ContentTag(it)
                    })
                    displayList.add(DisplayItemTag.FooterTag(programTables.size))
                    Resource.Success(displayList)
                }
            }
        }
    }
}