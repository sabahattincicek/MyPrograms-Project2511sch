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

/**
 * BroadcastReceiver responsible for handling alarm events triggered by the AlarmManager.
 * It manages both lesson reminders and absence check notifications.
 */
class AlarmReceiver: BroadcastReceiver() {

    private val TAG = "AlarmReceiver"
    private val TAG_DETAIL = "AlarmReceiverDetail"
    
    /**
     * Entry point interface for Hilt to provide dependencies within the BroadcastReceiver,
     * as BroadcastReceivers are instantiated by the system, not Hilt.
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmSchedulerEntryPoint{
        fun alarmScheduler(): IAlarmScheduler
        fun courseRepository(): ICourseRepository
        fun taskRepository(): ITaskRepository
    }

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * 
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "--- onReceive Triggered ---")
        Log.d(TAG_DETAIL, "Action received: $action")

        if (action == null) {
            Log.w(TAG_DETAIL, "Aborted: Action is null.")
            return
        }

        // Extract IDs from the intent extras
        val courseId = intent.getStringExtra(AlarmSchedulerImp.EXTRA_COURSE_ID)
        val taskId = intent.getStringExtra(AlarmSchedulerImp.EXTRA_TASK_ID)
        
        Log.d(TAG_DETAIL, "Extras extracted: [CourseID: $courseId, TaskID: $taskId]")

        if (courseId == null || taskId == null) {
            Log.e(TAG_DETAIL, "Aborted: Missing mandatory extras (CourseID or TaskID).")
            return
        }

        // Access Hilt dependencies via EntryPoint
        Log.v(TAG_DETAIL, "Accessing Hilt EntryPoint dependencies...")
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmSchedulerEntryPoint::class.java
        )
        val alarmScheduler = hiltEntryPoint.alarmScheduler()
        val courseRepository = hiltEntryPoint.courseRepository()
        val taskRepository = hiltEntryPoint.taskRepository()

        // Use goAsync to keep the receiver alive during background coroutine execution
        val pendingResult = goAsync() 

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG_DETAIL, "Fetching Course and Task details from database...")
                val courseResource = courseRepository.getById(courseId).first()
                val taskResource = taskRepository.getById(taskId).first()
                
                val course = courseResource.data
                val task = taskResource.data

                Log.d(TAG_DETAIL, "Fetch result: Course found = ${course != null}, Task found = ${task != null}")

                if (course != null && task != null) {
                    Log.d(TAG_DETAIL, "Validating activity status: CourseActive=${course.isActive}, TaskActive=${task.isActive}")
                    
                    // If either the course or the task is no longer active, we should not proceed
                    if (!course.isActive || !task.isActive) {
                        Log.i(TAG, "Target is inactive. Cancelling future alarms and skipping notification.")
                        alarmScheduler.cancel(course, task) 
                        return@launch
                    }

                    // Direct the flow based on the intent action
                    when(action){
                        AlarmSchedulerImp.ACTION_REMINDER -> {
                            Log.i(TAG, "Reminder shown for ${course.title} - ${task.title}")
                            showReminderNotification(context, course, task)
                            
                            // For recurring tasks, attempt to schedule the next occurrence
                            Log.d(TAG_DETAIL, "Triggering reschedule for the next occurrence.")
                            alarmScheduler.reschedule(course, task, action)
                        }

                        AlarmSchedulerImp.ACTION_ABSENCE_CHECK -> {
                            Log.i(TAG, "Absence Check shown for ${course.title} - ${task.title}")
                            showAbsenceCheckNotification(context, course, task)
                            
                            Log.d(TAG_DETAIL, "Triggering reschedule for the next occurrence.")
                            alarmScheduler.reschedule(course, task, action)
                        }
                        else -> {
                            Log.w(TAG, "Received an unexpected action: $action")
                        }
                    }
                } else {
                    Log.e(TAG, "Process failed: Course or Task not found in database for the provided IDs.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error encountered during alarm processing: ${e.message}", e)
            } finally {
                Log.d(TAG_DETAIL, "Completing async broadcast result.")
                pendingResult.finish()
                Log.i(TAG, "--- onReceive Process Finished ---")
            }
        }
    }

    /**
     * Constructs and displays a reminder notification for an upcoming task.
     * 
     * @param context The context for building the notification.
     * @param course The associated course.
     * @param task The task that is about to start.
     */
    private fun showReminderNotification(context: Context, course: Course, task: Task){
        Log.d(TAG_DETAIL, "Preparing Reminder Notification for Task: '${task.title}'")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification content
        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(course.title)
            .setContentText(context.getString(R.string.will_start_in_minutes,task.title, task.remindBefore ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationId = task.id.hashCode()
        Log.v(TAG_DETAIL, "Displaying Reminder Notification (ID: $notificationId)")
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Constructs and displays an absence check notification after a lesson.
     * 
     * @param context The context for building the notification.
     * @param course The associated course.
     * @param task The lesson task to check attendance for.
     */
    private fun showAbsenceCheckNotification(context: Context, course: Course, task: Task) {
        Log.d(TAG_DETAIL, "Preparing Absence Check Notification for Task: '${task.title}'")
        
        // Absence check is only relevant for Lesson types
        if (task !is Task.Lesson) {
            Log.w(TAG_DETAIL, "Aborted: Absence check requested for a non-lesson task.")
            return
        }

        // Unique ID for the absence check notification
        val notificationId = task.id.hashCode() + 1 

        // Intent for the 'Yes' (Attended) action
        Log.v(TAG_DETAIL, "Creating PendingIntent for 'ACTION_ATTENDED_YES'")
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_ATTENDED_YES"
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_TASK_ID", task.id)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context, notificationId, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for the 'No' (Missed) action
        Log.v(TAG_DETAIL, "Creating PendingIntent for 'ACTION_ATTENDED_NO'")
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_ATTENDED_NO"
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_TASK_ID", task.id)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification with actions
        val notification = NotificationCompat.Builder(context, "schedule_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.did_you_attend))
            .setContentText(context.getString(R.string.were_you_able_to_attend_the_lesson,course.title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.yes), yesPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.no), noPendingIntent)
            .setAutoCancel(true)
            .build()
        
        Log.v(TAG_DETAIL, "Displaying Absence Check Notification (ID: $notificationId)")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
