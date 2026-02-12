package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.course.DisplayItemCourse
import com.saboon.project_2511sch.presentation.task.DisplayItemTask
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

class GetCourseDisplayItemListUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
) {
    operator fun invoke(filter: FilterGeneric): Flow<Resource<List<DisplayItemCourse>>> {
        return combine(
            programTableRepository.getAll(),
            courseRepository.getAll(),
        ){programTableRes, courseRes ->
            if (programTableRes is Resource.Success && courseRes is Resource.Success){
                val allProgramTables = programTableRes.data ?: emptyList()
                val allCourses = courseRes.data ?: emptyList()

                val filteredCourse = when {
                    filter.programTable != null -> allCourses.filter { it.programTableId == filter.programTable.id } //get courses by program table
                    else -> allCourses //get all course
                }

                val programTableMap = allProgramTables.associateBy { it.id }

                val displayList = mutableListOf<DisplayItemCourse>()
                val groupedCourse = filteredCourse.groupBy { it.programTableId }
                groupedCourse.forEach { (programTableId, courseInGroup) ->
                    val programTable = programTableMap[programTableId]
                    if (programTable != null){
                        displayList.add(DisplayItemCourse.HeaderCourse(programTable.title))
                    }
                    courseInGroup.forEach { course ->
                        displayList.add(DisplayItemCourse.ContentCourse(
                            programTable = programTable!!,
                            course = course
                        ))
                    }
                }
                displayList.add(DisplayItemCourse.FooterCourse(filteredCourse.size))
                Resource.Success(displayList)
            }
            else if (programTableRes is Resource.Error || courseRes is Resource.Error){
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}