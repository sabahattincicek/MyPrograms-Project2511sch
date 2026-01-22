package com.saboon.project_2511sch.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saboon.project_2511sch.domain.usecase.course.CourseAbsenceUseCase
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class IncrementAbsenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val courseReadUseCase: CourseReadUseCase,
    private val courseAbsenceUseCase: CourseAbsenceUseCase,
): CoroutineWorker(appContext, workerParams) {

    private val tag = "IncrementAbsenceWorker"

    override suspend fun doWork(): Result {
        val courseId = inputData.getString("KEY_COURSE_ID")
        Log.d(tag, "Worker started for course ID: $courseId")

        if (courseId == null) {
            Log.e(tag, "Work failed: Course ID is null.")
            return Result.failure()
        }

        return try {
            Log.d(tag, "Fetching course from database...")
            val resource = courseReadUseCase.getById(courseId).first()

            if (resource is Resource.Success && resource.data != null) {
                Log.i(tag, "Course '${resource.data.title}' fetched successfully. Incrementing absence.")
                courseAbsenceUseCase.increment(resource.data)
                Log.i(tag, "Work finished successfully for course ID: $courseId")
                Result.success()
            } else {
                Log.e(tag, "Work failed: Could not fetch course. Reason: ${resource.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(tag, "An unexpected error occurred during work for course ID $courseId", e)
            Result.failure()
        }
    }

}
