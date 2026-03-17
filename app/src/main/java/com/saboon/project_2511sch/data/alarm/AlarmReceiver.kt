package com.saboon.project_2511sch.data.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.os.BundleCompat
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
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
        val action = intent.action ?: return
        val extras = intent.extras ?: return

        val course = BundleCompat.getParcelable(extras, AlarmSchedulerImp.EXTRA_COURSE, Course::class.java)
        val task = BundleCompat.getParcelable(extras, AlarmSchedulerImp.EXTRA_TASK, Task::class.java)

        if (course == null || task == null) return

        // Hilt EntryPoint ile Scheduler'a erişim (reschedule işlemi için)
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmSchedulerEntryPoint::class.java
        )
        val alarmScheduler = hiltEntryPoint.alarmScheduler()

        when(action){
            AlarmSchedulerImp.ACTION_REMINDER -> {
                // 1. Ders başlamadan önceki bildirim
                showReminderNotification(context, course, task)
                // 2. Periyodik ders ise bir sonraki haftayı planla
                alarmScheduler.reschedule(course, task)
            }

            AlarmSchedulerImp.ACTION_ABSENCE_CHECK -> {
                // 3. Ders bittikten sonraki yoklama bildirimi
                showAbsenceCheckNotification(context, course, task)
            }
        }
    }

    private fun showReminderNotification(context: Context, course: Course, task: Task){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(course.title)
            .setContentText("${task.title} ${task.remindBefore} dk sonra baslayacak.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(task.id.hashCode(), notification)
    }

    private fun showAbsenceCheckNotification(context: Context, course: Course, task: Task) {
        if (task !is Task.Lesson) return

        val notificationId = task.id.hashCode() + 1 // Reminder ID'si ile çakışmaması için +1

        //evet derse katildim butonu icin intent
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_ATTENDED_YES"
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_TASK_ID", task.id)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context, notificationId, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //hayir derse katilmadim butonu icin intent
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_ATTENDED_NO"
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_TASK_ID", task.id)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Derse Katildin mi?")
            .setContentText("'${course.title}' dersine katılabildin mi?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, "Evet", yesPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Hayır", noPendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

    }
}