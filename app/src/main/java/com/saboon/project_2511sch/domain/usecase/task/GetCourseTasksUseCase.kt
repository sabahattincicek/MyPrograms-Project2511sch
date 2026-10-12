package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.CourseTasks
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase that provides a course together with all of its tasks, for the course details screen.
 *
 * The course and its tasks are observed together, so the result updates when a task is added,
 * changed or removed and when the course itself is edited.
 * How the tasks are drawn (headers per task type, footer) is decided by the presentation layer.
 */
class GetCourseTasksUseCase @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    /**
     * @param course The course whose tasks are requested.
     * @return A Flow of the course in its current state and its tasks. When the course is not
     * found any more (it was just deleted), [course] itself is used.
     */
    operator fun invoke(course: Course): Flow<Resource<CourseTasks>> {
        return combine(
            courseRepository.getAll(),
            taskRepository.getAllByCourseId(course.id)
        ){courseRes, taskRes ->
            if (courseRes is Resource.Success){
                val currentCourse = courseRes.data?.firstOrNull { it.id == course.id } ?: course
                val tasks = taskRes.data ?: emptyList()
                Timber.d("Course tasks loaded: courseId=%s, tasks=%d", course.id, tasks.size)
                Resource.Success(CourseTasks(currentCourse, tasks))
            }
            else if (courseRes is Resource.Error || taskRes is Resource.Error){
                Timber.w("Course tasks could not be loaded: courseId=%s", course.id)
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}
