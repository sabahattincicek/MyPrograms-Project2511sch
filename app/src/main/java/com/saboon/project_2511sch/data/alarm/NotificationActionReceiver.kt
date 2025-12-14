package com.saboon.project_2511sch.data.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.saboon.project_2511sch.data.worker.IncrementAbsenceWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver(){
    private val tag = "NotificationActionReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(tag, "Action received by onReceive.")

        if (context == null || intent == null) {
            Log.e(tag, "Context or Intent is null, cannot proceed.")
            return
        }

        val courseId = intent.getStringExtra("KEY_COURSE_ID")
        val notificationId = intent.getIntExtra("KEY_NOTIFICATION_ID", 0)
        val action = intent.action

        Log.d(tag, "Action: $action, Course ID: $courseId, Notification ID: $notificationId")

        when(action){
            "ACTION_YES" -> {
                Log.i(tag, "'Yes' action selected. No further action needed.")
            }
            "ACTION_NO" -> {
                if (courseId != null){
                    Log.i(tag, "'No' action selected. Enqueuing worker to increment absence for course ID: $courseId")
                    val workRequest = OneTimeWorkRequestBuilder<IncrementAbsenceWorker>()
                        .setInputData(workDataOf("KEY_COURSE_ID" to courseId))
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                } else {
                    Log.w(tag, "'No' action selected, but courseId was null.")
                }
            }
            else -> {
                Log.w(tag, "Unknown or null action received: $action")
            }
        }

        // Dismiss the notification
        if (notificationId != 0) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.cancel(notificationId)
            Log.d(tag, "Notification with ID $notificationId has been cancelled.")
        }
    }
}