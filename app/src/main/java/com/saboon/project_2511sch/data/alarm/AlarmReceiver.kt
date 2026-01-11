package com.saboon.project_2511sch.data.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.BundleCompat
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class AlarmReceiver: BroadcastReceiver() {

    private val tag = "AlarmReceiver"
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmSchedulerEntryPoint{
        fun alarmScheduler(): IAlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Alarm received by onReceive with action: ${intent.action}")

        val extras = intent.extras ?: run {
            Log.e(tag, "Intent extras were null. Aborting.")
            return
        }
        val alarmAction = intent.action
        val programTable = BundleCompat.getParcelable(extras, "EXTRA_PROGRAM_TABLE", ProgramTable::class.java)
        val course = BundleCompat.getParcelable(extras, "EXTRA_COURSE", Course::class.java)
        val task = BundleCompat.getParcelable(extras, "EXTRA_SCHEDULE", Task::class.java)
        if (programTable == null || course == null || task == null) {
            Log.e(tag, "One or more of the parcelable objects were null. Aborting.")
            return
        }

        Log.i(tag, "Processing alarm for task: '${task.title}' (ID: ${task.id})")

        val hiltEntryPoint = EntryPointAccessors.fromApplication(context.applicationContext, AlarmSchedulerEntryPoint::class.java)
        val alarmScheduler = hiltEntryPoint.alarmScheduler()

        when(alarmAction){
            AlarmSchedulerImp.ACTION_REMINDER -> {
                Log.d(tag, "Handling a REMINDER alarm.")
                showReminderNotification(context, programTable, course, task)
                alarmScheduler.rescheduleReminder(programTable, course, task)
            }
            AlarmSchedulerImp.ACTION_ABSENCE_CHECK -> {
                Log.d(tag, "Handling an ABSENCE_CHECK alarm.")
                showAbsenceCheckNotification(context, programTable, course, task)
                alarmScheduler.rescheduleAbsenceReminder(programTable, course, task)
            }
            else -> {
                Log.w(tag, "Unknown or missing alarm action: $alarmAction")
            }
        }
    }

    private fun showReminderNotification(context: Context, programTable: ProgramTable, course: Course, task: Task){
//        val notificationId = task.id.hashCode()
//        val notificationManager = context.getSystemService(NotificationManager::class.java)
//        val notification = NotificationCompat.Builder(context, "schedule_reminders")
//            .setSmallIcon(R.drawable.baseline_add_24) // TODO: Change icon
//            .setContentTitle("Upcoming: ${task.title ?: "Event"}")
//            .setContentText("For course '${course.title ?: "your course"}' at ${task.startTime.toFormattedString("HH:mm")}.")
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText("Event: ${task.title}\nCourse: ${course.title}\nTime: ${task.startTime.toFormattedString("HH:mm")}\nDescription: ${task.description}")
//            )
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//        Log.i(tag, "Reminder notification posted with ID: $notificationId")
    }

    private fun showAbsenceCheckNotification(context: Context, programTable: ProgramTable, course: Course, task: Task) {
        val notificationId = task.id.hashCode() + 2 // Use a different ID for this notification type if needed

        // "Yes" action
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_YES"
            putExtra("KEY_NOTIFICATION_ID", notificationId)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId, // Unique request code for this action
            yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "No" action
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_NO"
            putExtra("KEY_COURSE_ID", course.id)
            putExtra("KEY_NOTIFICATION_ID", notificationId)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1, // CRITICAL: Must be a different request code!
            noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.baseline_add_24) // TODO: Change icon
            .setContentTitle("Attendance Check")
            .setContentText("Did you attend '${task.title}'?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Yes", yesPendingIntent)
            .addAction(0, "No", noPendingIntent)
            .setAutoCancel(true) // Dismiss the notification after a button is clicked
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
        Log.i(tag, "Absence check notification posted with ID: $notificationId")
    }
}