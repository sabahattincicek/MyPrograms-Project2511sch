package com.saboon.project_2511sch.data.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver: BroadcastReceiver() {

    private val tag = "AlarmReceiver"
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmSchedulerEntryPoint{
        fun alarmScheduler(): IAlarmScheduler
        fun courseRepository(): ICourseRepository
        fun taskRepository(): ITaskRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(tag, "onReceive: action=$action")

        if (action == null) {
            Log.w(tag, "Action is null, skipping")
            return
        }

        val courseId = intent.getStringExtra(AlarmSchedulerImp.EXTRA_COURSE_ID)
        val taskId = intent.getStringExtra(AlarmSchedulerImp.EXTRA_TASK_ID)
        
        Log.d(tag, "Extras received - CourseID: $courseId, TaskID: $taskId")

        if (courseId == null || taskId == null) {
            Log.e(tag, "Missing extras (courseId or taskId), aborting")
            return
        }

        // Hilt EntryPoint ile Repository'lere erişmek için
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmSchedulerEntryPoint::class.java
        )
        val alarmScheduler = hiltEntryPoint.alarmScheduler()
        val courseRepository = hiltEntryPoint.courseRepository()
        val taskRepository = hiltEntryPoint.taskRepository()

        // Veritabanı işlemi için Coroutine başlat
        val pendingResult = goAsync() // Receiver'ın hemen ölmemesi için

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(tag, "Fetching data from repositories...")
                val courseResource = courseRepository.getById(courseId).first()
                val taskResource = taskRepository.getById(taskId).first()
                
                val course = courseResource.data
                val task = taskResource.data

                Log.d(tag, "Data fetch result - Course found: ${course != null}, Task found: ${task != null}")

                if (course != null && task != null) {
                    Log.d(tag, "Checking activity status - Course active: ${course.isActive}, Task active: ${task.isActive}")
                    // bildirim gostermeden once hala aktif mi kontrol et
                    if (!course.isActive || !task.isActive) {
                        Log.d(tag, "Target is inactive, cancelling alarms and skipping notification")
                        alarmScheduler.cancel(course, task) // Artık gereksizse iptal et
                        return@launch
                    }

                    when(action){
                        AlarmSchedulerImp.ACTION_REMINDER -> {
                            Log.d(tag, "Action matched: ACTION_REMINDER")
                            // Ders başlamadan önceki bildirim
                            showReminderNotification(context, course, task)
                            // Periyodik ders ise bir sonraki haftayı planla
                            Log.d(tag, "Attempting to reschedule for next occurrence")
                            alarmScheduler.reschedule(course, task)
                        }

                        AlarmSchedulerImp.ACTION_ABSENCE_CHECK -> {
                            Log.d(tag, "Action matched: ACTION_ABSENCE_CHECK")
                            // Ders bittikten sonraki yoklama bildirimi
                            showAbsenceCheckNotification(context, course, task)
                        }
                        else -> {
                            Log.w(tag, "Unknown action: $action")
                        }
                    }
                } else {
                    Log.e(tag, "Could not proceed: course or task is null in DB")
                }
            }catch (e: Exception){
                Log.e(tag, "Error in AlarmReceiver: ${e.message}", e)
            }finally {
                Log.d(tag, "Finishing async broadcast result")
                pendingResult.finish()
            }
        }
    }

    private fun showReminderNotification(context: Context, course: Course, task: Task){
        Log.d(tag, "showReminderNotification for task: ${task.title}")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(course.title)
            .setContentText("${task.title} ${task.remindBefore} dk sonra baslayacak.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationId = task.id.hashCode()
        Log.d(tag, "Posting reminder notification with ID: $notificationId")
        notificationManager.notify(notificationId, notification)
    }

    private fun showAbsenceCheckNotification(context: Context, course: Course, task: Task) {
        Log.d(tag, "showAbsenceCheckNotification for task: ${task.title}")
        if (task !is Task.Lesson) {
            Log.w(tag, "Task is not a Lesson, skipping absence check notification")
            return
        }

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
        
        Log.d(tag, "Posting absence check notification with ID: $notificationId")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
