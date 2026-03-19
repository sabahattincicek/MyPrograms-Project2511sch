package com.saboon.project_2511sch.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BootReceiver is responsible for restoring all scheduled alarms after a device reboot.
 * In Android, scheduled alarms are cleared when the device is turned off.
 * This receiver listens for the BOOT_COMPLETED broadcast to reschedule them.
 */
@AndroidEntryPoint
class BootReceiver: BroadcastReceiver() {

    private val TAG = "BootReceiver"

    /**
     * Field injection is used here because BroadcastReceivers are instantiated by the 
     * Android system using a zero-argument constructor.
     */
    @Inject
    lateinit var taskRepository: ITaskRepository
    @Inject
    lateinit var courseRepository: ICourseRepository
    @Inject
    lateinit var alarmScheduler: IAlarmScheduler
    @Inject
    lateinit var settingsRepository: ISettingsRepository

    /**
     * Standard entry point for BroadcastReceivers.
     * 
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received (should be BOOT_COMPLETED or similar).
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "--- onReceive Triggered ---")
        val action = intent?.action
        Log.d(TAG, "Action received: $action")
        
        // Check if the received broadcast is for the device booting up
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.i(TAG, "Boot detected. Initiating alarm rescheduling process.")
            
            // goAsync() is used to prevent the receiver from being killed immediately
            // while we perform asynchronous database operations.
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Executing rescheduleAlarms in background coroutine...")
                    rescheduleAlarms()
                    Log.i(TAG, "Alarm rescheduling successfully completed.")
                } catch (e: Exception) {
                    Log.e(TAG, "Critical error during rescheduling: ${e.message}", e)
                } finally {
                    Log.v(TAG, "Finishing async broadcast pendingResult.")
                    pendingResult.finish()
                    Log.i(TAG, "--- BootReceiver Process Finished ---")
                }
            }
        } else {
            Log.w(TAG, "Received unexpected action: $action. Skipping.")
        }
    }

    /**
     * Fetches all active courses and tasks from the database and re-schedules them.
     */
    private suspend fun rescheduleAlarms() {
        Log.d(TAG, "Fetching data for rescheduling: Courses, Tasks, and Settings...")
        
        // Retrieve current data snapshots from repositories
        val coursesResource = courseRepository.getAllActive().first()
        val tasksResource = taskRepository.getAll().first()
        val isAbsenceReminderEnabled = settingsRepository.getAbsenceReminderEnabled().first()

        Log.v(TAG, "Repository results - Courses: ${coursesResource::class.java.simpleName}, Tasks: ${tasksResource::class.java.simpleName}")

        if (coursesResource is Resource.Success && tasksResource is Resource.Success) {
            val courses = coursesResource.data ?: emptyList()
            val tasks = tasksResource.data ?: emptyList()
            
            Log.i(TAG, "Data loaded successfully. Processing ${courses.size} courses and ${tasks.size} tasks.")
            Log.d(TAG, "Absence reminder feature status: $isAbsenceReminderEnabled")

            // Create a map of courses for efficient lookup by ID
            val courseMap = courses.associateBy { it.id }

            tasks.forEach { task ->
                val course = courseMap[task.courseId]
                if (course != null) {
                    Log.d(TAG, "Re-scheduling alarm for Task: '${task.title}' (ID: ${task.id})")
                    // The alarmScheduler.schedule handles internal activity checks
                    alarmScheduler.schedule(course, task)
                } else {
                    Log.w(TAG, "Warning: Associated course not found for Task: '${task.title}' (TaskID: ${task.id})")
                }
            }
        } else {
            // Determine which resource failed for better logging
            val courseError = (coursesResource as? Resource.Error)?.message ?: "Success/Loading"
            val taskError = (tasksResource as? Resource.Error)?.message ?: "Success/Loading"
            Log.e(TAG, "Failed to load necessary data for rescheduling. CourseError: $courseError, TaskError: $taskError")
        }
    }
}
