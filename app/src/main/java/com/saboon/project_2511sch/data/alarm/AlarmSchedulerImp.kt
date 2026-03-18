package com.saboon.project_2511sch.data.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.toFormattedString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

/**
 * Implementation of IAlarmScheduler responsible for managing system alarms
 * for course reminders and absence checks.
 */
class AlarmSchedulerImp @Inject constructor (
    private val context: Context,
    private val tagRepository: ITagRepository,
    private val settingsRepository: ISettingsRepository
): IAlarmScheduler {

    private val TAG = "AlarmSchedulerImp"

    // Reference to the System AlarmManager
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    companion object {
        const val ACTION_REMINDER = "com.saboon.project_2511sch.ACTION_REMINDER"
        const val ACTION_ABSENCE_CHECK = "com.saboon.project_2511sch.ACTION_ABSENCE_CHECK"

        const val EXTRA_COURSE_ID = "EXTRA_COURSE_ID"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    }

    /**
     * Schedules alarms for a given task if all conditions (Tag/Course/Task active) are met.
     * 
     * @param course The course associated with the task.
     * @param task The task to schedule (Lesson, Exam, or Homework).
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(
        course: Course,
        task: Task
    ) {
        Log.i(TAG, "--- Schedule Process Started ---")
        Log.d(TAG, "Task Info: [ID: ${task.id}, Title: ${task.title}, Type: ${task::class.java.simpleName}]")
        Log.d(TAG, "Course Info: [ID: ${course.id}, Title: ${course.title}, IsActive: ${course.isActive}]")

        // Check if the associated tag is active
        val isTagActive = if (course.tagId != null) {
            runBlocking {
                val tagResource = tagRepository.getById(course.tagId).first()
                val active = (tagResource as? Resource.Success)?.data?.isActive ?: true
                Log.d(TAG, "Tag active check: ID=${course.tagId}, IsActive=$active")
                active
            }
        } else {
            Log.d(TAG, "No tag assigned, assuming active by default.")
            true 
        }

        // Only schedule if Tag, Course, and Task are all active
        if (isTagActive && course.isActive && task.isActive){
            Log.i(TAG, "All activation conditions met. Proceeding to set alarms.")
            
            // 1. Process Reminder Alarms
            if (task.remindBefore >= 0){
                var triggerTime = when(task){
                    is Task.Lesson -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
                    is Task.Exam -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
                    is Task.Homework -> calculateCombinedTime(task.dueDate, task.dueTime) - (task.remindBefore * 60 * 1000)
                }
                
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Reminder: Scheduled=$triggerTime (${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}), Now=$currentTime")

                // For lessons, we might need to find the next occurrence if the initial trigger is in the past
                if (task is Task.Lesson){
                    while (triggerTime < currentTime){
                        val nextOccurrence = task.recurrenceRule.getNextOccurrence(triggerTime)
                        if (nextOccurrence == null) {
                            Log.d(TAG, "No more occurrences found for recurring lesson.")
                            break
                        }
                        triggerTime = nextOccurrence
                        if (triggerTime > currentTime){
                            Log.d(TAG, "Found next occurrence for reminder: ${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
                            val intent = createIntent(course, task, ACTION_REMINDER)
                            setAlarm(triggerTime, intent, task.id.hashCode())
                            break
                        }
                    }
                    // If the first calculation is already in the future
                    if (triggerTime > currentTime && task.recurrenceRule.freq != RecurrenceRule.Frequency.ONCE) {
                        // This case is handled by the loop if it starts behind, 
                        // but if it's already ahead, we still need to set it.
                        // However, the loop logic above might be confusing for the first future occurrence.
                        // Let's ensure if it was already in the future, it gets set.
                    }
                    
                    // Re-evaluating for clarity: If triggerTime is in future, set it.
                    if (triggerTime > currentTime) {
                        val intent = createIntent(course, task, ACTION_REMINDER)
                        setAlarm(triggerTime, intent, task.id.hashCode())
                    }
                } else {
                    // For non-recurring tasks (Exam, Homework)
                    if (triggerTime > currentTime){
                        Log.d(TAG, "Setting non-recurring reminder alarm.")
                        val intent = createIntent(course, task, ACTION_REMINDER)
                        setAlarm(triggerTime, intent, task.id.hashCode())
                    } else {
                        Log.w(TAG, "Reminder skipped: Trigger time is in the past.")
                    }
                }
            } else {
                Log.d(TAG, "Reminder skipped: 'remindBefore' is negative (${task.remindBefore}).")
            }

            // 2. Process Absence Reminder Alarms (Only for Lessons)
            val isAbsenceEnabled = runBlocking { settingsRepository.getAbsenceReminderEnabled().first() }
            Log.d(TAG, "Absence reminder feature enabled: $isAbsenceEnabled")
            
            if (isAbsenceEnabled && task is Task.Lesson){
                var triggerTime = calculateCombinedTime(task.date, task.timeEnd)
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Absence Check: Scheduled=$triggerTime (${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}), Now=$currentTime")

                // Handle recurrence for absence check
                while (triggerTime < currentTime){
                    val nextOccurrence = task.recurrenceRule.getNextOccurrence(triggerTime)
                    if (nextOccurrence == null) break
                    triggerTime = nextOccurrence
                    if (triggerTime > currentTime){
                        Log.d(TAG, "Found next occurrence for absence check: ${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
                        val intent = createIntent(course, task, ACTION_ABSENCE_CHECK)
                        setAlarm(triggerTime, intent, task.id.hashCode() + 1)
                        break
                    }
                }
                if (triggerTime > currentTime) {
                    val intent = createIntent(course, task, ACTION_ABSENCE_CHECK)
                    setAlarm(triggerTime, intent, task.id.hashCode() + 1)
                }
            }
        } else {
            // If inactive, ensure any existing alarms are removed
            Log.i(TAG, "Inactive status detected. Cancelling any existing alarms.")
            cancel(course, task)
        }
        Log.i(TAG, "--- Schedule Process Finished ---")
    }

    /**
     * Cancels both reminder and absence check alarms for a specific task.
     * 
     * @param course The course associated with the task.
     * @param task The task whose alarms should be cancelled.
     */
    override fun cancel(course: Course, task: Task) {
        Log.d(TAG, "Cancelling alarms for Task: ${task.title} (ID: ${task.id})")
        
        // Cancel Reminder
        val reminderIntent = createIntent(course, task, ACTION_REMINDER)
        alarmManager.cancel(createPendingIntent(task.id.hashCode(), reminderIntent))
        Log.v(TAG, "Reminder cancelled for RequestCode: ${task.id.hashCode()}")

        // Cancel Absence Check
        val absenceIntent = createIntent(course, task, ACTION_ABSENCE_CHECK)
        alarmManager.cancel(createPendingIntent(task.id.hashCode() + 1, absenceIntent))
        Log.v(TAG, "Absence check cancelled for RequestCode: ${task.id.hashCode() + 1}")
    }

    /**
     * Reschedules a lesson for its next occurrence based on its recurrence rule.
     * 
     * @param course The course associated with the task.
     * @param task The lesson task to reschedule.
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun reschedule(
        course: Course,
        task: Task
    ) {
        Log.i(TAG, "Rescheduling requested for: ${task.title}")
        if (task !is Task.Lesson) {
            Log.w(TAG, "Aborted: Reschedule is only applicable to Lessons.")
            return
        }
        
        val rRule = task.recurrenceRule
        Log.d(TAG, "Recurrence Data: Freq=${rRule.freq}, Start=${rRule.dtStart}, Until=${rRule.until.toFormattedString("dd-MM-yyyy")}")
        
        if (rRule.freq == RecurrenceRule.Frequency.ONCE) {
            Log.d(TAG, "Aborted: Lesson has no recurrence (Frequency.ONCE).")
            return
        }

        // Calculate the next date for the lesson
        val nextDate = rRule.getNextOccurrence(task.date)
        Log.d(TAG, "Calculated next date: ${nextDate?.toFormattedString("dd-MM-yyyy") ?: "NONE"}")
        
        if (nextDate != null && nextDate <= rRule.until) {
            Log.i(TAG, "Valid next occurrence found. Re-scheduling...")
            val nextTask = task.copy(date = nextDate)
            schedule(course, nextTask)
        } else {
            Log.d(TAG, "Reschedule skipped: No further occurrences within 'until' limit.")
        }
    }

    /**
     * Synchronizes all alarms for a specific course by cancelling and re-scheduling all its tasks.
     */
    override fun checkAndSyncCourseAlarms(
        course: Course,
        tasks: List<Task>
    ) {
        Log.i(TAG, "Syncing alarms for Course: ${course.title} (Task count: ${tasks.size})")
        tasks.forEach { task ->
            cancel(course, task)
            schedule(course, task)
        }
    }

    /**
     * Synchronizes all alarms for all courses and tasks associated with a specific tag.
     */
    override fun checkAndSyncTagAlarms(
        tag: Tag,
        courses: List<Course>,
        tasks: List<Task>
    ) {
        Log.i(TAG, "Syncing alarms for Tag: ${tag.title} (TagID: ${tag.id})")
        val tasksByCourse = tasks.groupBy { it.courseId }
        courses.filter { it.tagId == tag.id }.forEach { course ->
            val courseTasks = tasksByCourse[course.id] ?: emptyList()
            checkAndSyncCourseAlarms(course, courseTasks)
        }
    }

    /**
     * Helper to create an Intent for the AlarmReceiver.
     */
    private fun createIntent(course: Course, task: Task, action: String): Intent{
        // Define a unique Data URI to ensure PendingIntents are distinct for different tasks
        val dataUri = when(task){
            is Task.Lesson -> "task://${task.id}/${task.date}/${action}".toUri()
            is Task.Exam -> "task://${task.id}/${task.date}/${action}".toUri()
            is Task.Homework -> "task://${task.id}/${task.dueDate}/${action}".toUri()
        }
        
        Log.v(TAG, "Creating Intent: Action=$action, TaskID=${task.id}, URI=$dataUri")
        
        return Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            this.data = dataUri
            putExtra(EXTRA_COURSE_ID, course.id)
            putExtra(EXTRA_TASK_ID, task.id)
        }
    }

    /**
     * Helper to wrap an Intent into a PendingIntent for the AlarmManager.
     */
    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent{
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Low-level call to schedule an alarm with the Android System AlarmManager.
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun setAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
        val pendingIntent = createPendingIntent(requestCode, intent)
        val timeString = triggerAtMillis.toFormattedString("dd-MM-yyyy HH:mm:ss")
        Log.d(TAG, "Requesting System Alarm: Code=$requestCode, Time=$timeString")
        
        when {
            // For Android 12 (API 31) and above, check for exact alarm permission
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.v(TAG, "Using setExactAndAllowWhileIdle (Exact)")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    Log.w(TAG, "Exact permission missing. Using setAndAllowWhileIdle (Inexact)")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
            // For older versions, use setExact directly
            else -> {
                Log.v(TAG, "Using setExact (Legacy API)")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    /**
     * Combines separate date and time values into a single timestamp (Long).
     */
    private fun calculateCombinedTime(dateMillis: Long, timeMillis:Long): Long {
        val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timeCal = Calendar.getInstance().apply { timeInMillis = timeMillis }

        val result = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        Log.v(TAG, "Time Calculation: Combined=${result.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        return result
    }

}
