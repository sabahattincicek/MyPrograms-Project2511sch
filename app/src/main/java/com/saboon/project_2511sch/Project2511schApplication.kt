package com.saboon.project_2511sch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Project2511schApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "schedule_reminders", // Bu ID, AlarmReceiver'da kullanilanla AYNI OLMALI
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Shows notification for upcoming schedules."
        notificationManager.createNotificationChannel(channel)
    }
}