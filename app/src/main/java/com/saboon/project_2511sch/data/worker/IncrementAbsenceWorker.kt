package com.saboon.project_2511sch.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.task.TaskReadUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * A worker responsible for incrementing the absence count for a specific lesson.
 * It fetches the task from the repository, checks if it's a Lesson, 
 * and adds the current date to its absence list.
 */
@HiltWorker
class IncrementAbsenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskReadUseCase: TaskReadUseCase,
    private val taskWriteUseCase: TaskWriteUseCase
): CoroutineWorker(appContext, workerParams) {

    private val TAG = "IncrementAbsenceWorker"

    /**
     * Executes the background work to increment absence.
     * @return Result of the operation (Success, Failure, or Retry).
     */
    override suspend fun doWork(): Result {
        Log.i(TAG, "--- IncrementAbsenceWorker Process Started ---")
        
        // Retrieve the task ID from input data
        val taskId = inputData.getString("KEY_TASK_ID")
        
        // Log basic input verification
        if (taskId == null) {
            Log.e(TAG, "Worker failed: 'KEY_TASK_ID' is missing in inputData.")
            return Result.failure()
        }
        
        Log.d(TAG, "Target Task ID received: $taskId")

        return try {
            Log.d(TAG, "Fetching task from repository...")
            // Get the task by ID and wait for the first non-loading resource state
            val resource = taskReadUseCase.getById(taskId).first { it !is Resource.Loading }
            
            Log.d(TAG, "Resource state received: ${resource::class.java.simpleName}")

            // Check if the task was successfully fetched and if it is of type 'Lesson'
            if (resource is Resource.Success && resource.data is Task.Lesson) {
                val lesson = resource.data
                Log.i(TAG, "Lesson found: '${lesson.title}'. Initial absences: ${lesson.absence.size}")

                // Prepare updated absence list by adding the lesson's date
                val absenceDateList = lesson.absence.toMutableList()
                absenceDateList.add(lesson.date)
                
                // Create a copy of the lesson with the updated absence list
                val updatedTask = lesson.copy(
                    absence = absenceDateList
                )
                
                Log.d(TAG, "Sending update request to TaskWriteUseCase...")
                val updateResult = taskWriteUseCase.update(updatedTask)
                Log.d(TAG, "Update operation resource state: ${updateResult::class.java.simpleName}")

                // Evaluate the update result
                if (updateResult is Resource.Success){
                    Log.i(TAG, "Successfully updated absences for lesson ID: $taskId")
                    Result.success()
                } else {
                    // Log the error message if the resource is an Error type
                    val errorMsg = (updateResult as? Resource.Error)?.message ?: "Unknown error"
                    Log.w(TAG, "Update failed: $errorMsg. Retrying work...")
                    Result.retry()
                }
            } else {
                // Determine the reason for failure (Not found, Error state, or wrong type)
                val failureReason = when (resource) {
                    is Resource.Error -> "Repository error: ${resource.message}"
                    is Resource.Success -> "Task is not a Lesson (found: ${resource.data?.let { it::class.java.simpleName } ?: "null"})"
                    else -> "Unexpected resource state"
                }
                Log.e(TAG, "Process aborted: $failureReason")
                Result.failure()
            }
        } catch (e: Exception) {
            // Catch and log any unexpected exceptions during the process
            Log.e(TAG, "Critical failure in doWork: ${e.localizedMessage}", e)
            Result.failure()
        } finally {
            Log.i(TAG, "--- IncrementAbsenceWorker Process Finished ---")
        }
    }
}
