package com.saboon.project_2511sch.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saboon.project_2511sch.domain.usecase.task.LessonAbsenceWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * A worker that records an absence for a specific session of a lesson.
 *
 * It is enqueued by NotificationActionReceiver when the user answers "Missed" on the absence
 * check notification. The input carries the lesson id ([KEY_TASK_ID]) and the date of the
 * session the notification belonged to ([KEY_SESSION_DATE]); that date is what gets stored,
 * so the absence list shows when the lesson was actually missed.
 *
 * The update itself is done by [LessonAbsenceWriteUseCase], the same code path the manual
 * "+" button uses.
 */
@HiltWorker
class IncrementAbsenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val lessonAbsenceWriteUseCase: LessonAbsenceWriteUseCase
): CoroutineWorker(appContext, workerParams) {

    /**
     * Executes the background work to record the absence.
     * @return Success when saved, Failure when the input is incomplete or the lesson is gone,
     * Retry when saving failed for another reason.
     */
    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID)
        if (taskId == null) {
            Timber.e("Worker failed: '%s' is missing in inputData.", KEY_TASK_ID)
            return Result.failure()
        }
        // Work enqueued by an older app version carries no session date; fall back to now.
        val sessionDate = inputData.getLong(KEY_SESSION_DATE, System.currentTimeMillis())
        Timber.i("Recording absence: taskId=%s, sessionDate=%d", taskId, sessionDate)

        return try {
            when (val result = lessonAbsenceWriteUseCase.add(taskId, sessionDate)) {
                is Resource.Success -> {
                    Timber.i("Absence recorded: taskId=%s", taskId)
                    Result.success()
                }
                else -> {
                    Timber.w("Absence not recorded: taskId=%s, message=%s", taskId, result.message)
                    // A missing lesson will not come back, so retrying only makes sense for other errors.
                    if (result.message == LessonAbsenceWriteUseCase.LESSON_NOT_FOUND) Result.failure()
                    else Result.retry()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Critical failure in doWork: taskId=%s", taskId)
            Result.failure()
        }
    }

    companion object {
        const val KEY_TASK_ID = "KEY_TASK_ID"
        const val KEY_SESSION_DATE = "KEY_SESSION_DATE"
    }
}
