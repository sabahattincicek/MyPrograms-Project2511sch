package com.saboon.project_2511sch.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskReadUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class IncrementAbsenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskReadUseCase: TaskReadUseCase,
    private val taskWriteUseCase: TaskWriteUseCase
): CoroutineWorker(appContext, workerParams) {

    private val TAG = "IncrementAbsenceWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork started")
        val taskId = inputData.getString("KEY_TASK_ID")
        if (taskId == null) {
            Log.e(TAG, "taskId is null, returning failure")
            return Result.failure()
        }
        Log.d(TAG, "Processing taskId: $taskId")

        return try {
            val resource = taskReadUseCase.getById(taskId).first { it !is Resource.Loading }
            Log.d(TAG, "Task fetched, resource: ${resource::class.java.simpleName}")

            if (resource is Resource.Success && resource.data is Task.Lesson) {
                val lesson = resource.data
                Log.d(TAG, "Task is a Lesson. Current absences: ${lesson.absence.size}")

                val absenceDateList = lesson.absence.toMutableList()
                absenceDateList.add(lesson.date)
                val updatedTask = lesson.copy(
                    absence = absenceDateList
                )
                
                Log.d(TAG, "Updating task with new absence date")
                val updateResult = taskWriteUseCase.update(updatedTask)
                Log.d(TAG, "Update result: ${updateResult::class.java.simpleName}")

                if (updateResult is Resource.Success){
                    Log.d(TAG, "Absence incremented successfully")
                    Result.success()
                }else {
                    Log.w(TAG, "Update failed, retrying...")
                    Result.retry()
                }
            } else {
                Log.e(TAG, "Task not found or not a Lesson type. Resource: ${resource.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in doWork: ${e.message}", e)
            Result.failure()
        }
    }

}
