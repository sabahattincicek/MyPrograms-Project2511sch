package com.saboon.project_2511sch.domain.model

/**
 * A single recorded absence of a lesson, flattened out of [Task.Lesson.absence].
 *
 * A lesson stores its absences as a plain list of timestamps. This model pairs each of those
 * timestamps with the lesson it belongs to, so that the absences of a whole course can be
 * listed together (for example in the absence dates dialog of the course details screen).
 *
 * @property lessonId Id of the lesson the absence belongs to.
 * @property lessonTitle Title of that lesson, shown next to the date.
 * @property date Unix timestamp (milliseconds) stored for the absence.
 */
data class AbsenceRecord(
    val lessonId: String,
    val lessonTitle: String,
    val date: Long
)
