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

/**
 * A BroadcastReceiver that handles user interactions with notifications.
 * It processes actions like "Attended" or "Missed" from the absence check notification.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    private val TAG = "NotificationActionReceiver"

    /**
     * Standard entry point for BroadcastReceivers.
     * 
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received, containing action and task data.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "--- onReceive Triggered ---")

        // Basic null safety check for context and intent
        if (context == null || intent == null) {
            Log.w(TAG, "Process aborted: Context or Intent is null.")
            return
        }

        // Extract action and extras sent from AlarmReceiver
        val action = intent.action
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", 0)
        val taskId = intent.getStringExtra("EXTRA_TASK_ID")

        Log.d(TAG, "Received Intent Details - Action: $action, NotificationID: $notificationId, TaskID: $taskId")

        // Route the logic based on the user's selection
        when (action) {
            "ACTION_ATTENDED_YES" -> {
                Log.i(TAG, "User selected: YES (Attended). No background task required.")
                // No action needed for 'Yes' currently
            }
            "ACTION_ATTENDED_NO" -> {
                Log.i(TAG, "User selected: NO (Missed). Initiating absence increment process.")
                
                // If the user reports missing the lesson, trigger background work to update the database
                if (taskId != null) {
                    Log.d(TAG, "Enqueuing IncrementAbsenceWorker for LessonID: $taskId")
                    
                    val workRequest = OneTimeWorkRequestBuilder<IncrementAbsenceWorker>()
                        .setInputData(workDataOf("KEY_TASK_ID" to taskId))
                        .build()

                    WorkManager.getInstance(context).enqueue(workRequest)
                    Log.v(TAG, "IncrementAbsenceWorker successfully enqueued.")
                } else {
                    Log.e(TAG, "Critical failure: User reported a missed lesson, but TaskID is missing from intent.")
                }
            }
            else -> {
                Log.w(TAG, "Received an unknown action: $action")
            }
        }

        // Always dismiss the notification from the tray once an action is taken
        if (notificationId != 0) {
            Log.d(TAG, "Requesting NotificationManager to cancel notification ID: $notificationId")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            Log.v(TAG, "Notification $notificationId removed from tray.")
        } else {
            Log.w(TAG, "Warning: NotificationID is 0, skipping cancellation.")
        }

        Log.i(TAG, "--- onReceive Process Finished ---")
    }
}
