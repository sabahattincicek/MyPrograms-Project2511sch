package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.AbsenceRecord
import com.saboon.project_2511sch.domain.model.Task
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase that turns the tasks of a course into the list of its recorded absences.
 *
 * Only lessons can have absences, so exams and homeworks are ignored. Every timestamp in
 * [Task.Lesson.absence] becomes one [AbsenceRecord]; duplicates are kept because each entry
 * counts as one missed session. The use case is pure (no repository access): the caller
 * already holds the task list, so the result always matches what is shown on screen.
 */
class GetCourseAbsenceRecordsUseCase @Inject constructor() {

    /**
     * Collects the absences of all lessons in [tasks].
     * @return Absence records sorted from newest to oldest, then by lesson title.
     */
    operator fun invoke(tasks: List<Task>): List<AbsenceRecord> {
        val records = tasks
            .filterIsInstance<Task.Lesson>()
            .flatMap { lesson ->
                lesson.absence.map { date -> AbsenceRecord(lesson.id, lesson.title, date) }
            }
            .sortedWith(compareByDescending<AbsenceRecord> { it.date }.thenBy { it.lessonTitle })
        Timber.d("Absence records built: tasks=%d, records=%d", tasks.size, records.size)
        return records
    }
}
