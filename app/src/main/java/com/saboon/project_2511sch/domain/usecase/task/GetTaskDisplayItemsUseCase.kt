package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.task.TaskDisplayItem
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTaskDisplayItemsUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
){
    operator fun invoke(course: Course): Flow<Resource<List<TaskDisplayItem>>> {
        return taskRepository.getAllTasksByCourseId(course.id).map { resource ->
            when(resource){
                is Resource.Error -> Resource.Error(resource.message ?: "Unknown error")
                is Resource.Idle -> Resource.Idle()
                is Resource.Loading -> Resource.Loading()
                is Resource.Success -> {
                    val tasks = resource.data ?: emptyList()
                    val displayItems = mutableListOf<TaskDisplayItem>()
                    val groupedTasks = tasks.groupBy { it::class }
                    groupedTasks.forEach { (taskClass, tasksInGroup) ->
                        val headerTitle = taskClass.simpleName ?: "Other"
                        displayItems.add(TaskDisplayItem.HeaderItem(headerTitle))
                        tasksInGroup.forEach { task ->
                            displayItems.add(
                                TaskDisplayItem.ContentItem(
                                    task = task,
                                    occurrenceId = task.id
                                )
                            )
                        }
                    }
                    Resource.Success(displayItems.toList())
                }
            }
        }
    }

}