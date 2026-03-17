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

class AlarmSchedulerImp @Inject constructor (
    private val context: Context,
    private val tagRepository: ITagRepository,
    private val settingsRepository: ISettingsRepository
): IAlarmScheduler {

    private val TAG = "AlarmSchedulerImp"

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    companion object {
        const val ACTION_REMINDER = "com.saboon.project_2511sch.ACTION_REMINDER"
        const val ACTION_ABSENCE_CHECK = "com.saboon.project_2511sch.ACTION_ABSENCE_CHECK"

        const val EXTRA_COURSE_ID = "EXTRA_COURSE_ID"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(
        course: Course,
        task: Task
    ) {
        Log.d(TAG, "--- Schedule Process Started ---")
        Log.d(TAG, "Task Info: [ID: ${task.id}, Title: ${task.title}, Type: ${task::class.java.simpleName}]")
        Log.d(TAG, "Course Info: [ID: ${course.id}, Title: ${course.title}, IsActive: ${course.isActive}]")

        val isTagActive = if (course.tagId != null) {
            runBlocking {
                val tagResource = tagRepository.getById(course.tagId).first()
                val active = (tagResource as? Resource.Success)?.data?.isActive ?: true
                Log.d(TAG, "Tag check: ID=${course.tagId}, IsActive=$active")
                active
            }
        } else {
            Log.d(TAG, "No tag assigned to this course, assuming tag is active")
            true 
        }

        if (isTagActive && course.isActive && task.isActive){
            Log.d(TAG, "All conditions met (Tag, Course, Task are active).")
            
            // 1. REMINDER
            if (task.remindBefore >= 0){
                val triggerTime = when(task){
                    is Task.Lesson -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
                    is Task.Exam -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
                    is Task.Homework -> calculateCombinedTime(task.dueDate, task.dueTime) - (task.remindBefore * 60 * 1000)
                }
                
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Reminder: Trigger=${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}, Now=${currentTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
                
                if (triggerTime > currentTime){
                    val intent = createIntent(course, task, ACTION_REMINDER)
                    setAlarm(triggerTime, intent, task.id.hashCode())
                } else {
                    Log.w(TAG, "Reminder NOT scheduled because trigger time is in the past.")
                }
            } else {
                Log.d(TAG, "Reminder skipped because remindBefore is negative (${task.remindBefore})")
            }

            // 2. ABSENCE REMINDER (Only for Lesson)
            val isAbsenceEnabled = runBlocking { settingsRepository.getAbsenceReminderEnabled().first() }
            Log.d(TAG, "Absence check enabled in settings: $isAbsenceEnabled")
            
            if (isAbsenceEnabled && task is Task.Lesson){
                val triggerTime = calculateCombinedTime(task.date, task.timeEnd)
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Absence Check: Trigger=${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}, Now=${currentTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
                
                if (triggerTime > currentTime){
                    val intent = createIntent(course, task, ACTION_ABSENCE_CHECK)
                    setAlarm(triggerTime, intent, task.id.hashCode() + 1)
                } else {
                    Log.w(TAG, "Absence Check NOT scheduled because trigger time is in the past.")
                }
            }
        } else {
            Log.d(TAG, "Scheduling skipped. Reasons: TagActive=$isTagActive, CourseActive=${course.isActive}, TaskActive=${task.isActive}")
            cancel(course, task)
        }
        Log.d(TAG, "--- Schedule Process Finished ---")
    }

    override fun cancel(course: Course, task: Task) {
        Log.d(TAG, "Cancelling alarms for Task: ${task.title} (ID: ${task.id})")
        
        // Cancel Reminder
        val reminderIntent = createIntent(course, task, ACTION_REMINDER)
        val reminderPI = createPendingIntent(task.id.hashCode(), reminderIntent)
        alarmManager.cancel(reminderPI)
        Log.d(TAG, "Reminder alarm cancelled (RequestCode: ${task.id.hashCode()})")

        // Cancel Absence Check
        val absenceIntent = createIntent(course, task, ACTION_ABSENCE_CHECK)
        val absencePI = createPendingIntent(task.id.hashCode() + 1, absenceIntent)
        alarmManager.cancel(absencePI)
        Log.d(TAG, "Absence check alarm cancelled (RequestCode: ${task.id.hashCode() + 1})")
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun reschedule(
        course: Course,
        task: Task
    ) {
        Log.d(TAG, "Reschedule requested for Task: ${task.title}")
        if (task !is Task.Lesson) {
            Log.d(TAG, "Reschedule aborted: Not a Lesson type.")
            return
        }
        
        val rRule = task.recurrenceRule
        Log.d(TAG, "Recurrence Rule: Freq=${rRule.freq}, Interval=${rRule.dtStart}, Until=${rRule.until.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        
        if (rRule.freq == RecurrenceRule.Frequency.ONCE) {
            Log.d(TAG, "Reschedule aborted: Frequency is ONCE.")
            return
        }

        val nextDate = rRule.getNextOccurrence(task.date)
        Log.d(TAG, "Next occurrence calculated: ${nextDate?.toFormattedString("dd-MM-yyyy") ?: "NONE"}")
        
        if (nextDate != null && nextDate <= rRule.until) {
            Log.d(TAG, "Next occurrence is within limits. Scheduling...")
            val nextTask = task.copy(date = nextDate)
            schedule(course, nextTask)
        } else {
            Log.d(TAG, "No more occurrences to schedule or limit reached.")
        }
    }

    override fun checkAndSyncCourseAlarms(
        course: Course,
        tasks: List<Task>
    ) {
        Log.d(TAG, "Syncing alarms for Course: ${course.title} (${tasks.size} tasks)")
        tasks.forEach { task ->
            cancel(course, task)
            schedule(course, task)
        }
    }

    override fun checkAndSyncTagAlarms(
        tag: Tag,
        courses: List<Course>,
        tasks: List<Task>
    ) {
        Log.d(TAG, "Syncing alarms for Tag: ${tag.title} (TagID: ${tag.id})")
        val tasksByCourse = tasks.groupBy { it.courseId }
        courses.filter { it.tagId == tag.id }.forEach { course ->
            val courseTasks = tasksByCourse[course.id] ?: emptyList()
            checkAndSyncCourseAlarms(course, courseTasks)
        }
    }

    private fun createIntent(course: Course, task: Task, action: String): Intent{
        val dataUri = when(task){
            is Task.Lesson -> "task://${task.id}/${task.date}/${action}".toUri()
            is Task.Exam -> "task://${task.id}/${task.date}/${action}".toUri()
            is Task.Homework -> "task://${task.id}/${task.dueDate}/${action}".toUri()
        }
        Log.v(TAG, "Creating Intent: Action=$action, Data=$dataUri, CourseID=${course.id}, TaskID=${task.id}")
        
        return Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            this.data = dataUri
            putExtra(EXTRA_COURSE_ID, course.id)
            putExtra(EXTRA_TASK_ID, task.id)
        }
    }

    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent{
        Log.v(TAG, "Creating PendingIntent: RequestCode=$requestCode")
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun setAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
        val pendingIntent = createPendingIntent(requestCode, intent)
        val formattedTime = triggerAtMillis.toFormattedString("dd-MM-yyyy HH:mm:ss")
        Log.d(TAG, "Setting system alarm at $formattedTime (RequestCode: $requestCode)")
        
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "Using setExactAndAllowWhileIdle")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    Log.w(TAG, "Exact alarm permission NOT granted. Falling back to setAndAllowWhileIdle (inexact).")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
            else -> {
                Log.d(TAG, "Using setExact (Legacy API)")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

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
        
        Log.v(TAG, "calculateCombinedTime: Date=${dateMillis.toFormattedString("dd-MM-yyyy")}, Time=${timeMillis.toFormattedString("HH:mm")}, Result=${result.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        return result
    }

}
