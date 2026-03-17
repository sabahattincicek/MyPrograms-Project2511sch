package com.saboon.project_2511sch.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver responsible for restoring all alarms after a device reboot.
 * Without this, all scheduled notifications would be lost when the phone restarts.
 */
class BootReceiver @Inject constructor(
    private val taskRepository: ITaskRepository,
    private val courseRepository: ICourseRepository,
    private val alarmScheduler: IAlarmScheduler,
    private val settigsRepository: ISettingsRepository
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if the received broadcast is for the device booting up
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Use goAsync() to keep the receiver alive while we do background DB work
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAlarms()
                }catch (e: Exception){

                }finally {
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * Fetches all tasks and their respective courses to re-register them in the system.
     */
    private suspend fun rescheduleAlarms(){
        // Fetch current snapshots of data from repositories
        val coursesResource = courseRepository.getAllActive().first()
        val tasksResource = taskRepository.getAll().first()
        val isAbsenceReminderEnabled = settigsRepository.getAbsenceReminderEnabled().first()

        if (coursesResource is Resource.Success && tasksResource is Resource.Success){
            val courses = coursesResource.data ?: emptyList()
            val tasks = tasksResource.data ?: emptyList()

            // Map courses by ID for efficient lookup
            val courseMap = courses.associateBy { it.id }

            tasks.forEach { task ->
                val course = courseMap[task.courseId]
                if (course != null){
                    if (task.remindBefore >= 0) alarmScheduler.schedule(course, task)
                    if (task is Task.Lesson && isAbsenceReminderEnabled) alarmScheduler.scheduleAbsenceCheck(course, task)
                }
            }
        }
    }
}