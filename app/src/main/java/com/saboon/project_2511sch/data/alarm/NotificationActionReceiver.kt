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

    private val TAG = "NotificationActionReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.w(TAG, "onReceive: context or intent is null")
            return
        }

        // AlarmReceiver'dan gelen verileri alıyoruz
        val action = intent.action
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", 0)
        val taskId = intent.getStringExtra("EXTRA_TASK_ID")

        Log.d(TAG, "onReceive: action=$action, notificationId=$notificationId, taskId=$taskId")

        when(action){
            "ACTION_ATTENDED_YES" -> {
                Log.d(TAG, "Action: ACTION_ATTENDED_YES")
            }//DO NOTHING
            "ACTION_ATTENDED_NO" -> {
                Log.d(TAG, "Action: ACTION_ATTENDED_NO")
                // Kullanıcı derse katılmadığını beyan etti.
                // Arka planda devamsızlık sayısını artıracak Worker'ı tetikliyoruz.
                if (taskId != null){
                    Log.d(TAG, "Enqueuing IncrementAbsenceWorker for taskId: $taskId")
                    val workRequest = OneTimeWorkRequestBuilder<IncrementAbsenceWorker>()
                        .setInputData(workDataOf("KEY_TASK_ID" to taskId))
                        .build()

                    WorkManager.getInstance(context).enqueue(workRequest)
                }else{
                    Log.e(TAG, "Missed lesson reported but taskId is null!")
                }
            }
        }

        // Hangi butona basılırsa basılsın bildirimi ekrandan kaldırıyoruz
        if (notificationId != 0){
            Log.d(TAG, "Cancelling notification: $notificationId")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    }
}
