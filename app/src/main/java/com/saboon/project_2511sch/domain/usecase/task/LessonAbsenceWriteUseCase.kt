package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase for adding and removing the absences of a lesson.
 *
 * It is the single place where [Task.Lesson.absence] is changed, shared by the manual
 * "+" / "-" buttons (home and course details screens) and the "Missed" action of the
 * absence check notification.
 *
 * The lesson is always re-read from the repository by id instead of trusting an object
 * handed over by the UI. The home screen works with per-week copies of a lesson whose
 * `date` is the date of that week; saving such a copy would overwrite the lesson's real
 * start date. Re-reading guarantees that only the absence list changes.
 */
class LessonAbsenceWriteUseCase @Inject constructor(
    private val taskRepository: ITaskRepository,
    private val taskWriteUseCase: TaskWriteUseCase
) {
    /**
     * Records an absence on [date] for the lesson with [lessonId].
     * The same date may be recorded more than once; each entry counts as one missed session.
     */
    suspend fun add(lessonId: String, date: Long): Resource<Task> {
        Timber.d("Add absence: lessonId=%s, date=%d", lessonId, date)
        return when (val lesson = loadLesson(lessonId)) {
            is Resource.Success -> {
                val current = lesson.data!!
                taskWriteUseCase.update(current.copy(absence = current.absence + date))
            }
            else -> Resource.Error(lesson.message ?: LESSON_NOT_FOUND)
        }
    }

    /**
     * Removes one recorded absence on [date] from the lesson with [lessonId].
     * Returns [Resource.Error] and leaves the lesson untouched when no such absence exists.
     */
    suspend fun remove(lessonId: String, date: Long): Resource<Task> {
        Timber.d("Remove absence: lessonId=%s, date=%d", lessonId, date)
        return when (val lesson = loadLesson(lessonId)) {
            is Resource.Success -> {
                val current = lesson.data!!
                if (date !in current.absence) {
                    Timber.w("Absence not found: lessonId=%s, date=%d", lessonId, date)
                    return Resource.Error(ABSENCE_NOT_FOUND)
                }
                // minus(element) drops only the first match, so a duplicate date stays recorded once less.
                taskWriteUseCase.update(current.copy(absence = current.absence - date))
            }
            else -> Resource.Error(lesson.message ?: LESSON_NOT_FOUND)
        }
    }

    /**
     * Reads the current state of the lesson from the repository.
     * Returns [Resource.Error] when the task cannot be loaded or is not a lesson.
     */
    private suspend fun loadLesson(lessonId: String): Resource<Task.Lesson> {
        val resource = taskRepository.getById(lessonId).first { it !is Resource.Loading }
        val lesson = (resource as? Resource.Success)?.data as? Task.Lesson
        if (lesson == null) {
            Timber.w("Lesson could not be loaded: lessonId=%s, message=%s", lessonId, resource.message)
            return Resource.Error(resource.message ?: LESSON_NOT_FOUND)
        }
        return Resource.Success(lesson)
    }

    companion object {
        const val LESSON_NOT_FOUND = "Lesson not found"
        const val ABSENCE_NOT_FOUND = "Absence not found"
    }
}
