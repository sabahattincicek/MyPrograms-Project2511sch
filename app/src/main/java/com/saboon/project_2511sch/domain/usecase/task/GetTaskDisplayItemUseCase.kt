package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.task.DisplayItemTask
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
class GetTaskDisplayItemUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    operator fun invoke(filter: FilterGeneric): Flow<Resource<List<DisplayItemTask>>> {
        return combine(
            programTableRepository.getAll(),
            courseRepository.getAll(),
            taskRepository.getAll()
        ){programTableRes, courseRes, taskRes ->
            if (programTableRes is Resource.Success && courseRes is Resource.Success){
                val allProgramTables = programTableRes.data ?: emptyList()
                val allCourses = courseRes.data ?: emptyList()
                val allTasks = taskRes.data ?: emptyList()

                val filteredTasks = when {
                    // Eğer Course verilmişse: Sadece o kursun tasklarını getir
                    filter.course != null -> allTasks.filter { it.courseId == filter.course.id }
                    // Course yok ama ProgramTable verilmişse: Sadece o tablonun tasklarını getir
                    filter.programTable != null -> allTasks.filter { it.programTableId == filter.programTable.id }
                    // İkisi de yoksa: Tüm taskları getir
                    else -> allTasks
                }

                val tableMap = allProgramTables.associateBy { it.id }
                val courseMap = allCourses.associateBy { it.id }

                val displayList = mutableListOf<DisplayItemTask>()
                val groupedTasks = filteredTasks.groupBy { it::class.simpleName ?: "Other" }
                groupedTasks.forEach { (type, taskInGroup) ->
                    displayList.add(DisplayItemTask.HeaderTask(type))
                    taskInGroup.forEach { task ->
                        val programTable = tableMap[task.programTableId]
                        val course = courseMap[task.courseId]
                        if (programTable != null && course != null){
                            displayList.add(DisplayItemTask.ContentTask(programTable, course, task))
                        }
                    }
                }
                displayList.add(DisplayItemTask.FooterTask(filteredTasks.size))
                Resource.Success(displayList)
            }
            else if (programTableRes is Resource.Error || courseRes is Resource.Error || taskRes is Resource.Error){
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}