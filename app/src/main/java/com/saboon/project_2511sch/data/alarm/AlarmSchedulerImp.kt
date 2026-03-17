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
import com.saboon.project_2511sch.data.repository.SettingsRepositoryImp
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

class AlarmSchedulerImp @Inject constructor (
    private val context: Context,
    private val tagRepository: ITagRepository,
    private val settingsRepository: ISettingsRepository
): IAlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    companion object {
        const val ACTION_REMINDER = "com.saboon.project_2511sch.ACTION_REMINDER"
        const val ACTION_ABSENCE_CHECK = "com.saboon.project_2511sch.ACTION_ABSENCE_CHECK"

        const val EXTRA_COURSE = "EXTRA_COURSE"
        const val EXTRA_TASK = "EXTRA_TASK"
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(
        course: Course,
        task: Task
    ) {
        // Ders aktif degilse veya task aktif degilse veya no reminder (-1) ise kurma
        if (!course.isActive || !task.isActive || task.remindBefore < 0) return
        // derse ait tag var ise ve aktif degilse kurma
        if (course.tagId != null){
            val isTagActive = runBlocking {
                val tagResource = tagRepository.getById(course.tagId).first()
                (tagResource as? Resource.Success)?.data?.isActive ?: true
            }
            if (!isTagActive) return
        }

        val triggerTime = when(task){
            is Task.Lesson -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
            is Task.Exam -> calculateCombinedTime(task.date, task.timeStart) - (task.remindBefore * 60 * 1000)
            is Task.Homework -> calculateCombinedTime(task.dueDate, task.dueTime) - (task.remindBefore * 60 * 1000)
        }
        if (triggerTime > System.currentTimeMillis()){
            val intent = createIntent(course, task, ACTION_REMINDER)
            setAlarm(triggerTime, intent, task.id.hashCode())
        }
    }

    override fun cancel(course: Course, task: Task) {
        // Reminder'ı iptal et
        val reminderIntent = createIntent(course, task, ACTION_REMINDER)
        alarmManager.cancel(createPendingIntent(task.id.hashCode(), reminderIntent))
        // Absence Check'i iptal et
        val absenceIntent = createIntent(course, task, ACTION_ABSENCE_CHECK)
        alarmManager.cancel(createPendingIntent(task.id.hashCode() + 1, absenceIntent))
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun scheduleAbsenceCheck(
        course: Course,
        task: Task
    ) {
        // Ders aktif degilse veya task aktif degilse veya task.Lesson degil ise kurma
        if (!course.isActive || !task.isActive || task !is Task.Lesson) return
        // derse ait tag var ise ve aktif degilse kurma
        if (course.tagId != null){
            val isTagActive = runBlocking {
                val tagResource = tagRepository.getById(course.tagId).first()
                (tagResource as? Resource.Success)?.data?.isActive ?: true
            }
            if (!isTagActive) return
        }
        // ayarlarda absence reminder aktif edilmemisse kurma
        val isAbsenceEnabled = runBlocking {settingsRepository.getAbsenceReminderEnabled().first()}
        if (!isAbsenceEnabled) return

        val triggerTime = calculateCombinedTime(task.date, task.timeEnd)
        if (triggerTime > System.currentTimeMillis()){
            val intent = createIntent(course, task, ACTION_ABSENCE_CHECK)
            setAlarm(triggerTime, intent, task.id.hashCode() + 1)
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun reschedule(
        course: Course,
        task: Task
    ) {
        if (task !is Task.Lesson) return
        val rRule = task.recurrenceRule
        if (rRule.freq == RecurrenceRule.Frequency.ONCE) return

        val nextDate = rRule.getNextOccurrence(task.date)
        if (nextDate != null && nextDate <= rRule.until) {
            val nextTask = task.copy(date = nextDate)
            schedule(course, nextTask)
        }
    }

    private fun createIntent(course: Course?, task: Task, action: String): Intent{
        return Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            // Farklı görevlerin alarmlarının çakışmaması için URI ekliyoruz (Android kuralı)
            this.data = "task://${task.id}/${action}".toUri()
            putExtra(EXTRA_COURSE, course)
            putExtra(EXTRA_TASK, task)
        }
    }
    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent{
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
        when {
            // Android 12+ (API 31+) kontrolü
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    // İzin varsa: Tam vaktinde ve uykudayken bile çal
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // İzin yoksa: Uygulama çökmesin diye "yaklaşık" bir alarm kurar (Sistem kaydırabilir)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.w("AlarmSchedulerImp", "Exact alarm permission not granted, setting inexact alarm.")
                }
            }
            // Daha eski cihazlar
            else -> {
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

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

}