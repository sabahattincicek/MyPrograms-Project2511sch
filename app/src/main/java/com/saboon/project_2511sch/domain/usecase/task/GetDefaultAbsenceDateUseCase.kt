package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.RecurrenceRule
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * UseCase that suggests the date to preselect when the user adds an absence by hand.
 *
 * The suggestion is the most recent session of the lesson that has already started, because
 * that is the session a user most likely wants to mark as missed. The user can still pick
 * any other date in the date picker.
 *
 * Sessions are stepped with [java.util.Calendar] rather than [RecurrenceRule.getNextOccurrence],
 * which depends on the Android ICU calendar and therefore cannot run in JVM unit tests.
 */
class GetDefaultAbsenceDateUseCase @Inject constructor() {

    /**
     * Returns the start of the latest session of [lesson] at or before [now].
     * Falls back to the lesson's own date when it has not started yet or does not repeat.
     */
    operator fun invoke(lesson: Task.Lesson, now: Long): Long {
        val rule = lesson.recurrenceRule
        val field = when (rule.freq) {
            RecurrenceRule.Frequency.ONCE -> return lesson.date
            RecurrenceRule.Frequency.DAILY -> Calendar.DAY_OF_YEAR
            RecurrenceRule.Frequency.WEEKLY -> Calendar.WEEK_OF_YEAR
            RecurrenceRule.Frequency.MONTHLY -> Calendar.MONTH
            RecurrenceRule.Frequency.YEARLY -> Calendar.YEAR
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = lesson.date }
        var latest = lesson.date
        while (true) {
            calendar.add(field, 1)
            val next = calendar.timeInMillis
            if (next > now || next > rule.until) break
            latest = next
        }
        Timber.d("Default absence date: lessonId=%s, date=%d", lesson.id, latest)
        return latest
    }
}
