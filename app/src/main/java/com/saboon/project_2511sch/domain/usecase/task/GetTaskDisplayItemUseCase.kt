package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.task.DisplayItemTask
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
/**
 * UseCase responsible for fetching and formatting tasks for a specific Course.
 * It transforms raw data into a list of DisplayItemTask which includes headers and footers.
 */
class GetTaskDisplayItemUseCase @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    /**
     * Executes the UseCase.
     * @param course The specific course for which tasks are being requested.
     * @return A Flow of Resource containing a list of DisplayItemTask (Headers, Content, Footer).
     */
    operator fun invoke(course: Course): Flow<Resource<List<DisplayItemTask>>> {
        // Combine flows from both repositories to reactively update the UI when data changes
        return combine(
            courseRepository.getAll(),
            taskRepository.getAllByCourseId(course.id)
        ){courseRes, taskRes ->
            // Check if data retrieval was successful
            if (courseRes is Resource.Success){
                val allCourses = courseRes.data ?: emptyList()
                val allTasks = taskRes.data ?: emptyList()

                // "Early Return" Pattern: If there are no tasks, return an empty list immediately.
                // This triggers the "Empty State" UI in the Fragment without showing headers/footers.
                if (allTasks.isEmpty()) {
                    return@combine Resource.Success(emptyList())
                }

                // Map courses by ID for quick O(1) lookup during task iteration
                val courseMap = allCourses.associateBy { it.id }

                val displayList = mutableListOf<DisplayItemTask>()

                // Group tasks by their class type (Lesson, Exam, or Homework)
                // to create sectioned headers in the RecyclerView.
                val groupedTasks = allTasks.groupBy { it::class.simpleName ?: "Other" }
                groupedTasks.forEach { (type, taskInGroup) ->
                    // Add a Header for each group (e.g., "Lesson", "Exam")
                    displayList.add(DisplayItemTask.HeaderTask(type))
                    taskInGroup.forEach { task ->
                        val course = courseMap[task.courseId]
                        if (course != null){
                            // Add the actual task content associated with its course info
                            displayList.add(DisplayItemTask.ContentTask(course, task))
                        }
                    }
                }
                // Add a Footer to the end of the list, usually showing the total task count
                displayList.add(DisplayItemTask.FooterTask(allTasks.size))
                Resource.Success(displayList)
            }
            else if (courseRes is Resource.Error || taskRes is Resource.Error){
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}