package com.saboon.project_2511sch.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import java.util.Calendar

class AlarmSchedulerImp(
    private val context: Context
): IAlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private val tag = "AlarmSchedulerImp"

    companion object {
        const val ACTION_REMINDER = "com.saboon.project_2511sch.ACTION_REMINDER"
        const val ACTION_ABSENCE_CHECK = "com.saboon.project_2511sch.ACTION_ABSENCE_CHECK"
    }

    override fun scheduleReminder(programTable: ProgramTable, course: Course, task: Task) {
        Log.d(tag, "[Initial Task] Setting REMINDER for '${task.title}'")

        val triggerAtMillis = calculateCombinedTime(task.date, task.startTime) - (task.remindBefore * 60 * 1000)

        if (task.remindBefore > -1 && triggerAtMillis > System.currentTimeMillis()) {
            val reminderIntent = createIntent(programTable, course, task).apply {
                action = ACTION_REMINDER
                data = "task://${task.id}#reminder".toUri()
            }
            setAlarm(triggerAtMillis, reminderIntent, task.id.hashCode())
        } else {
            Log.w(tag, "[Initial Task] Reminder alarm SKIPPED for '${task.title}'.")
        }
    }

    override fun scheduleAbsenceReminder(programTable: ProgramTable, course: Course, task: Task) {
        Log.d(tag, "[Initial Task] Setting ABSENCE CHECK for '${task.title}'")
        val triggerAtMillis = calculateCombinedTime(task.date, task.endTime)

        if (triggerAtMillis > System.currentTimeMillis()) {
            val absenceIntent = createIntent(programTable, course, task).apply {
                action = ACTION_ABSENCE_CHECK
                data = "task://${task.id}#absence".toUri()
            }
            setAlarm(triggerAtMillis, absenceIntent, task.id.hashCode() + 1)
        } else {
            Log.w(tag, "[Initial Task] Absence alarm SKIPPED for '${task.title}'.")
        }
    }

    override fun rescheduleReminder(programTable: ProgramTable, course: Course, currentTask: Task) {
        val nextTriggerDate = calculateNextTriggerDate(currentTask) ?: return
        val nextSchedule = currentTask.copy(date = nextTriggerDate)

        Log.d(tag, "[Reschedule] Setting next REMINDER for '${nextSchedule.title}'")

        scheduleReminder(programTable, course, nextSchedule)
    }

    override fun rescheduleAbsenceReminder(programTable: ProgramTable, course: Course, currentTask: Task) {
        val nextTriggerDate = calculateNextTriggerDate(currentTask) ?: return
        val nextSchedule = currentTask.copy(date = nextTriggerDate)

        Log.d(tag, "[Reschedule] Setting next ABSENCE CHECK for '${nextSchedule.title}'")

        scheduleAbsenceReminder(programTable, course, nextSchedule)
    }

    override fun cancel(task: Task) {
        Log.d(tag, "Attempting to cancel all alarms for task: '${task.title}' (ID: ${task.id})")
        // Cancel reminder alarm
        val reminderIntent = createIntent(null, null, null).apply {
            action = ACTION_REMINDER
            data = "task://${task.id}#reminder".toUri()
        }
        alarmManager.cancel(createPendingIntent(task.id.hashCode(), reminderIntent))

        // Cancel absence check alarm
        val absenceIntent = createIntent(null, null, null).apply {
            action = ACTION_ABSENCE_CHECK
            data = "task://${task.id}#absence".toUri()
        }
        alarmManager.cancel(createPendingIntent(task.id.hashCode() + 1, absenceIntent))
    }

    private fun createIntent(programTable: ProgramTable?, course: Course?, task: Task?): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_PROGRAM_TABLE", programTable)
            putExtra("EXTRA_COURSE", course)
            putExtra("EXTRA_SCHEDULE", task)
        }
    }

    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(triggerAtMillis: Long, intent: Intent, requestCode: Int) {
        val pendingIntent = createPendingIntent(requestCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            Log.i(tag, "Setting EXACT alarm for request code $requestCode at $triggerAtMillis with action ${intent.action}")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            Log.i(tag, "Setting INEXACT alarm for request code $requestCode at $triggerAtMillis with action ${intent.action}")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun calculateCombinedTime(dateMillis: Long, timeMillis: Long): Long {
        val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timeCal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return Calendar.getInstance().apply {
            set(
                dateCal.get(Calendar.YEAR),
                dateCal.get(Calendar.MONTH),
                dateCal.get(Calendar.DAY_OF_MONTH),
                timeCal.get(Calendar.HOUR_OF_DAY),
                timeCal.get(Calendar.MINUTE),
                0
            )
        }.timeInMillis
    }

    private fun calculateNextTriggerDate(task: Task): Long? {
        if (task.recurrenceRule.isBlank()) {
            Log.d(tag, "No recurrence rule. Not rescheduling.")
            return null
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = task.date }
        return when (task.recurrenceRule) {
            "FREQ=DAILY" -> calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            "FREQ=WEEKLY" -> calendar.apply { add(Calendar.WEEK_OF_YEAR, 1) }.timeInMillis
            "FREQ=MONTHLY" -> calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            "FREQ=YEARLY" -> calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis
            else -> null
        }
    }
}