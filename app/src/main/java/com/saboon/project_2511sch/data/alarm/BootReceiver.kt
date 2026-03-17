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
 * Receiver responsible for restoring all alarms after a device reboot.
 * Without this, all scheduled notifications would be lost when the phone restarts.
 */
@AndroidEntryPoint
class BootReceiver: BroadcastReceiver() {

    private val TAG = "SBootReceiver"

    // Android işletim sistemi BroadcastReceiver sınıflarını (BootReceiver gibi) kendisi
    // oluşturur ve her zaman "boş bir constructor" (zero-argument constructor) bekler.
    // O sebeple constructor injection yerine field injection islemi yapilir
    @Inject
    lateinit var taskRepository: ITaskRepository
    @Inject
    lateinit var courseRepository: ICourseRepository
    @Inject
    lateinit var alarmScheduler: IAlarmScheduler
    @Inject
    lateinit var settingsRepository: ISettingsRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called with action: ${intent?.action}")
        // Check if the received broadcast is for the device booting up
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Boot detected, starting reschedule process")
            // Use goAsync() to keep the receiver alive while we do background DB work
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAlarms()
                    Log.d(TAG, "rescheduleAlarms successfully executed")
                }catch (e: Exception){
                    Log.e(TAG, "Error in rescheduleAlarms: ${e.message}", e)
                }finally {
                    Log.d(TAG, "Finishing pendingResult")
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * Fetches all tasks and their respective courses to re-register them in the system.
     */
    private suspend fun rescheduleAlarms(){
        Log.d(TAG, "rescheduleAlarms: Fetching active courses, tasks, and settings")
        // Fetch current snapshots of data from repositories
        val coursesResource = courseRepository.getAllActive().first()
        val tasksResource = taskRepository.getAll().first()
        val isAbsenceReminderEnabled = settingsRepository.getAbsenceReminderEnabled().first()

        Log.d(TAG, "rescheduleAlarms resources: courses=${coursesResource::class.java.simpleName}, tasks=${tasksResource::class.java.simpleName}")

        if (coursesResource is Resource.Success && tasksResource is Resource.Success){
            val courses = coursesResource.data ?: emptyList()
            val tasks = tasksResource.data ?: emptyList()
            Log.d(TAG, "rescheduleAlarms: Success. Processing ${courses.size} courses and ${tasks.size} tasks. Absence reminder enabled: $isAbsenceReminderEnabled")

            // Map courses by ID for efficient lookup
            val courseMap = courses.associateBy { it.id }

            tasks.forEach { task ->
                val course = courseMap[task.courseId]
                if (course != null){
                    if (task.remindBefore >= 0) {
                        Log.d(TAG, "Scheduling alarm for task: ${task.title}")
                        alarmScheduler.schedule(course, task)
                    }
                    if (task is Task.Lesson && isAbsenceReminderEnabled) {
                        Log.d(TAG, "Scheduling absence check for lesson: ${task.title}")
                        alarmScheduler.scheduleAbsenceCheck(course, task)
                    }
                } else {
                    Log.w(TAG, "Course not found for task id: ${task.id}")
                }
            }
        } else {
            Log.e(TAG, "rescheduleAlarms: Failed to fetch data from repositories")
        }
    }
}
