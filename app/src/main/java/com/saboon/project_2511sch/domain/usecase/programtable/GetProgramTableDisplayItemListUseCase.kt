package com.saboon.project_2511sch.domain.usecase.programtable

import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.presentation.programtable.DisplayItemProgramTable
import javax.inject.Inject
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProgramTableDisplayItemListUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository
) {
    operator fun invoke(): Flow<Resource<List<DisplayItemProgramTable>>> {
        return programTableRepository.getAll().map { resource ->
            when(resource) {
                is Resource.Error -> {Resource.Error(resource.message ?: "An Unknown error occurred")}
                is Resource.Idle -> {Resource.Idle()}
                is Resource.Loading -> {Resource.Loading()}
                is Resource.Success -> {
                    val programTables = resource.data ?: emptyList()

                    if (programTables.isEmpty()) {
                        return@map Resource.Success(emptyList())
                    }

                    val displayList = mutableListOf<DisplayItemProgramTable>()
                    displayList.addAll(programTables.map {
                        DisplayItemProgramTable.ContentProgramTable(it)
                    })
                    displayList.add(DisplayItemProgramTable.FooterProgramTable(programTables.size))
                    Resource.Success(displayList)
                }
            }
        }
    }
}