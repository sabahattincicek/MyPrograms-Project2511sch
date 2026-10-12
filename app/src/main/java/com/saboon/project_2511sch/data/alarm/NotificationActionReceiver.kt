package com.saboon.project_2511sch.data.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.saboon.project_2511sch.data.worker.IncrementAbsenceWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * A BroadcastReceiver that handles user interactions with notifications.
 * It processes actions like "Attended" or "Missed" from the absence check notification.
 *
 * "Missed" records an absence for the session the notification was shown for. The session
 * date is read from [EXTRA_SESSION_DATE], which [AlarmReceiver] attaches when it builds the
 * notification. The database update itself is handed to [IncrementAbsenceWorker], because a
 * receiver must return quickly.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {


    /**
     * Standard entry point for BroadcastReceivers.
     * 
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received, containing action and task data.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("--- onReceive Triggered ---")

        // Basic null safety check for context and intent
        if (context == null || intent == null) {
            Timber.w("Process aborted: Context or Intent is null.")
            return
        }

        // Extract action and extras sent from AlarmReceiver
        val action = intent.action
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", 0)
        val taskId = intent.getStringExtra("EXTRA_TASK_ID")
        // Notifications shown by an older app version carry no session date; fall back to now.
        val sessionDate = intent.getLongExtra(EXTRA_SESSION_DATE, System.currentTimeMillis())

        Timber.d("Received Intent Details - Action: $action, NotificationID: $notificationId, TaskID: $taskId")

        // Route the logic based on the user's selection
        when (action) {
            "ACTION_ATTENDED_YES" -> {
                Timber.i("User selected: YES (Attended). No background task required.")
                // No action needed for 'Yes' currently
            }
            "ACTION_ATTENDED_NO" -> {
                Timber.i("User selected: NO (Missed). Initiating absence increment process.")
                
                // If the user reports missing the lesson, trigger background work to update the database
                if (taskId != null) {
                    Timber.d("Enqueuing IncrementAbsenceWorker for LessonID: $taskId")
                    
                    val workRequest = OneTimeWorkRequestBuilder<IncrementAbsenceWorker>()
                        .setInputData(
                            workDataOf(
                                IncrementAbsenceWorker.KEY_TASK_ID to taskId,
                                IncrementAbsenceWorker.KEY_SESSION_DATE to sessionDate
                            )
                        )
                        .build()

                    WorkManager.getInstance(context).enqueue(workRequest)
                    Timber.v("IncrementAbsenceWorker successfully enqueued.")
                } else {
                    Timber.e("Critical failure: User reported a missed lesson, but TaskID is missing from intent.")
                }
            }
            else -> {
                Timber.w("Received an unknown action: $action")
            }
        }

        // Always dismiss the notification from the tray once an action is taken
        if (notificationId != 0) {
            Timber.d("Requesting NotificationManager to cancel notification ID: $notificationId")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            Timber.v("Notification $notificationId removed from tray.")
        } else {
            Timber.w("Warning: NotificationID is 0, skipping cancellation.")
        }

        Timber.i("--- onReceive Process Finished ---")
    }

    companion object {
        const val EXTRA_SESSION_DATE = "EXTRA_SESSION_DATE"
    }
}
