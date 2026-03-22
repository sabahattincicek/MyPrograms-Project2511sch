package com.saboon.project_2511sch.data.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.saboon.project_2511sch.presentation.widget.WidgetHome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            WidgetHome().updateAll(applicationContext)
            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }
    companion object {
        fun enqueueUpdate(context: Context){
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            // UniqueWork kullanarak üst üste binen istekleri engelliyoruz
            // REPLACE politikası ile son gelen istek her zaman geçerli olur
            WorkManager.getInstance(context).enqueueUniqueWork(
                "widget_home_update",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}