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

    private val TAG_MAIN = "AlarmSchedulerMain"
    private val TAG_DETAIL = "AlarmSchedulerDetail"
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    companion object {
        const val ACTION_REMINDER = "com.saboon.project_2511sch.ACTION_REMINDER"
        const val ACTION_ABSENCE_CHECK = "com.saboon.project_2511sch.ACTION_ABSENCE_CHECK"
        const val EXTRA_COURSE_ID = "EXTRA_COURSE_ID"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"

        private const val TYPE_REMINDER = 0
        private const val TYPE_ABSENCE = 1
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(course: Course, task: Task) {
        Log.d(TAG_DETAIL, "Starting schedule for Course: ${course.title}, Task: ${task.title} (ID: ${task.id})")
        
        val isTagActive = if (course.tagId != null) {
            runBlocking {
                val tagResource = tagRepository.getById(course.tagId).first()
                val active = (tagResource as? Resource.Success)?.data?.isActive ?: true
                Log.d(TAG_DETAIL, "Checking Tag ID: ${course.tagId}, IsActive: $active")
                active
            }
        } else {
            Log.d(TAG_DETAIL, "No tag assigned to course, assuming active.")
            true
        }

        if (isTagActive && course.isActive && task.isActive) {
            Log.d(TAG_DETAIL, "All active checks passed (Tag, Course, Task). Proceeding with scheduling.")
            scheduleReminder(course, task)

            if (task is Task.Lesson) {
                val isAbsenceEnabled = runBlocking { settingsRepository.getAbsenceReminderEnabled().first() }
                Log.d(TAG_DETAIL, "Absence reminder setting: $isAbsenceEnabled")
                if (isAbsenceEnabled) {
                    scheduleAbsenceCheck(course, task)
                }
            }
        } else {
            Log.i(TAG_MAIN, "Scheduling cancelled for Task '${task.title}' because it or its group is inactive.")
            Log.d(TAG_DETAIL, "Inactivity check: TagActive=$isTagActive, CourseActive=${course.isActive}, TaskActive=${task.isActive}. Cancelling alarms.")
            cancel(course, task)
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleReminder(course: Course, task: Task) {
        if (task.remindBefore < 0) {
            Log.d(TAG_DETAIL, "Reminder skipped for Task ${task.id}: remindBefore is negative.")
            return
        }

        val currentTime = System.currentTimeMillis()
        var triggerTime = when(task) {
            is Task.Lesson -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
            is Task.Exam -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
            is Task.Homework -> calculateCombinedTime(task.dueDate, task.dueTime) - (task.remindBefore * 60 * 1000)
        }

        Log.d(TAG_DETAIL, "Initial reminder trigger calculation: ${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")

        if (task is Task.Lesson) {
            while (triggerTime <= currentTime) {
                val nextOccurrence = task.recurrenceRule.getNextOccurrence(triggerTime)
                if (nextOccurrence == null || nextOccurrence == triggerTime) {
                    Log.d(TAG_DETAIL, "No more future occurrences found for Task ${task.id} reminder.")
                    break
                }
                triggerTime = nextOccurrence
                Log.d(TAG_DETAIL, "Recalculated reminder occurrence: ${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
            }
        }

        if (triggerTime > currentTime) {
            val intent = createIntent(course, task, ACTION_REMINDER)
            setAlarm(triggerTime, intent, getRequestCode(task.id, TYPE_REMINDER))
            Log.i(TAG_MAIN, "Exact alarm set for REMINDER: Course '${course.title}', Task '${task.title}' at ${triggerTime.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        } else {
            Log.w(TAG_DETAIL, "Reminder NOT set for Task ${task.id}: trigger time is in the past.")
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleAbsenceCheck(course: Course, task: Task.Lesson) {
        val currentTime = System.currentTimeMillis()
        var absenceTrigger = calculateCombinedTime(task.date, task.timeEnd)

        Log.d(TAG_DETAIL, "Initial absence check trigger calculation: ${absenceTrigger.toFormattedString("dd-MM-yyyy HH:mm:ss")}")

        while (absenceTrigger <= currentTime) {
            val nextOccurrence = task.recurrenceRule.getNextOccurrence(absenceTrigger)
            if (nextOccurrence == null || nextOccurrence == absenceTrigger) {
                Log.d(TAG_DETAIL, "No more future occurrences found for Task ${task.id} absence check.")
                break
            }
            absenceTrigger = nextOccurrence
            Log.d(TAG_DETAIL, "Recalculated absence check occurrence: ${absenceTrigger.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        }

        if (absenceTrigger > currentTime) {
            val intent = createIntent(course, task, ACTION_ABSENCE_CHECK)
            setAlarm(absenceTrigger, intent, getRequestCode(task.id, TYPE_ABSENCE))
            Log.i(TAG_MAIN, "Exact alarm set for ABSENCE CHECK: Course '${course.title}', Task '${task.title}' at ${absenceTrigger.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        } else {
            Log.w(TAG_DETAIL, "Absence check NOT set for Task ${task.id}: trigger time is in the past.")
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun reschedule(course: Course, task: Task, firedAction: String) {
        if (task !is Task.Lesson || task.recurrenceRule.freq == RecurrenceRule.Frequency.ONCE) {
            Log.d(TAG_DETAIL, "Reschedule skipped for Task ${task.id}: Not a lesson or freq is ONCE.")
            return
        }

        Log.d(TAG_DETAIL, "Rescheduling process triggered by action: $firedAction for Task ID: ${task.id}")
        when (firedAction) {
            ACTION_REMINDER -> scheduleReminder(course, task)
            ACTION_ABSENCE_CHECK -> scheduleAbsenceCheck(course, task)
        }
    }

    private fun getRequestCode(taskId: String, type: Int): Int {
        val rc = taskId.hashCode() * 31 + type
        Log.v(TAG_DETAIL, "Generated RequestCode: $rc (TaskID: $taskId, Type: $type)")
        return rc
    }

    private fun createIntent(course: Course, task: Task, action: String): Intent {
        val dataUri = "task://${task.id}/${action}".toUri()
        Log.v(TAG_DETAIL, "Creating Intent: Action=$action, Data=$dataUri")
        return Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            this.data = dataUri
            putExtra(EXTRA_COURSE_ID, course.id)
            putExtra(EXTRA_TASK_ID, task.id)
        }
    }

    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun setAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
        val pendingIntent = createPendingIntent(requestCode, intent)
        Log.d(TAG_DETAIL, "Setting alarm in AlarmManager: RequestCode=$requestCode, TargetTime=${triggerAtMillis.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun calculateCombinedTime(dateMillis: Long, timeMillis: Long): Long {
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
        Log.v(TAG_DETAIL, "Combined time: ${result.toFormattedString("dd-MM-yyyy HH:mm:ss")}")
        return result
    }

    override fun cancel(course: Course, task: Task) {
        Log.i(TAG_MAIN, "Cancelling all alarms for Task '${task.title}' of Course '${course.title}'")
        
        val reminderRC = getRequestCode(task.id, TYPE_REMINDER)
        val reminderIntent = createIntent(course, task, ACTION_REMINDER)
        alarmManager.cancel(createPendingIntent(reminderRC, reminderIntent))
        Log.d(TAG_DETAIL, "Reminder alarm cancelled for Task ${task.id} (RC: $reminderRC)")

        val absenceRC = getRequestCode(task.id, TYPE_ABSENCE)
        val absenceIntent = createIntent(course, task, ACTION_ABSENCE_CHECK)
        alarmManager.cancel(createPendingIntent(absenceRC, absenceIntent))
        Log.d(TAG_DETAIL, "Absence check alarm cancelled for Task ${task.id} (RC: $absenceRC)")
    }

    override fun checkAndSyncCourseAlarms(course: Course, tasks: List<Task>) {
        Log.d(TAG_DETAIL, "Syncing alarms for Course: '${course.title}' (Task count: ${tasks.size})")
        tasks.forEach { schedule(course, it) }
    }

    override fun checkAndSyncTagAlarms(tag: Tag, courses: List<Course>, tasks: List<Task>) {
        Log.d(TAG_DETAIL, "Syncing alarms for Tag: '${tag.title}'")
        val tasksByCourse = tasks.groupBy { it.courseId }
        courses.filter { it.tagId == tag.id }.forEach { course ->
            tasksByCourse[course.id]?.forEach { schedule(course, it) }
        }
    }

    override fun syncAbsenceAlarms(isEnabled: Boolean, courses: List<Course>, tasks: List<Task>) {
        Log.d(TAG_DETAIL, "Syncing absence alarms globally. Enabled: $isEnabled")
        val tasksByCourse = tasks.groupBy { it.courseId }

        courses.forEach { course ->
            val courseTasks = tasksByCourse[course.id] ?: emptyList()

            courseTasks.filterIsInstance<Task.Lesson>().forEach { lesson ->
                if (isEnabled) {
                    val isTagActive = if (course.tagId != null) {
                        runBlocking {
                            val tagResource = tagRepository.getById(course.tagId).first()
                            (tagResource as? Resource.Success)?.data?.isActive ?: true
                        }
                    } else true

                    if (isTagActive && course.isActive && lesson.isActive) {
                        scheduleAbsenceCheck(course, lesson)
                    }
                } else {
                    val absenceRC = getRequestCode(lesson.id, TYPE_ABSENCE)
                    val absenceIntent = createIntent(course, lesson, ACTION_ABSENCE_CHECK)
                    alarmManager.cancel(createPendingIntent(absenceRC, absenceIntent))
                    Log.d(TAG_MAIN, "Explicitly cancelled absence check for Lesson ${lesson.id} during global sync.")
                    Log.d(TAG_DETAIL, "Explicitly cancelled absence check for Lesson ${lesson.id} during global sync.")
                }
            }
        }
    }
}
