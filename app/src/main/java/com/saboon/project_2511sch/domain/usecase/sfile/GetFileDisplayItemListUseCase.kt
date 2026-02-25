package com.saboon.project_2511sch.domain.usecase.sfile

import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.sfile.DisplayItemSFile
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFileDisplayItemListUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository
) {
    operator fun invoke(filter: FilterGeneric): Flow<Resource<List<DisplayItemSFile>>>{
        return sFileRepository.getAll().map { resource ->
            when(resource) {
                is Resource.Error -> {Resource.Error(resource.message ?: "Bilinmeyen Hata")}
                is Resource.Idle -> {Resource.Idle()}
                is Resource.Loading -> {Resource.Loading()}
                is Resource.Success -> {
                    val allSFiles = resource.data ?: emptyList()
                    val filteredSFiles = allSFiles.filter { sFile ->
                        when{
                            filter.task != null -> {
                                sFile.taskId == filter.task.id
                            }
                            filter.course != null ->{
                                val isSameCourse = sFile.courseId == filter.course.id
                                if (filter.courseIncludeSubItems) isSameCourse
                                else isSameCourse && sFile.taskId == null
                            }
                            filter.programTable != null -> {
                                val isSameProgramTable = sFile.programTableId == filter.programTable.id
                                if (filter.programTableIncludeSubItems) isSameProgramTable
                                else isSameProgramTable && sFile.courseId == null && sFile.taskId == null
                            }
                            else -> true
                        }
                    }

                    if (filteredSFiles.isEmpty()) {
                        return@map Resource.Success(emptyList())
                    }

                    val displayList = mutableListOf<DisplayItemSFile>()
                    displayList.addAll(filteredSFiles.map { sFile ->
                        DisplayItemSFile.ContentSFile(
                            programTable = filter.programTable,
                            course = filter.course,
                            task = filter.task,
                            sFile = sFile
                        )
                    })
                    displayList.add(DisplayItemSFile.FooterSFile(filteredSFiles.size))
                    Resource.Success(displayList)
                }
            }
        }
    }
}