package com.saboon.project_2511sch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ApplicationInfo
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point of MyPrograms.
 *
 * Responsibilities:
 * - Acts as the root of the Hilt dependency graph ([HiltAndroidApp]).
 * - Provides the WorkManager [Configuration] so that workers
 *   (e.g. IncrementAbsenceWorker, WidgetUpdateWorker) can receive injected dependencies
 *   through [HiltWorkerFactory].
 * - Sets up logging by planting a Timber tree. A tree is planted only in debuggable builds,
 *   so release builds produce no log output.
 * - Creates the notification channel used by lesson reminders.
 */
@HiltAndroidApp
class Project2511schApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * Called once when the process starts. Initializes logging first so that everything
     * after it can log, then creates the notification channel.
     */
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        createNotificationChannel()
    }

    /**
     * Plants a [Timber.DebugTree] when the app is debuggable. Without a planted tree
     * every Timber call is a no-op, which keeps release builds silent.
     */
    private fun setupLogging() {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber planted")
        }
    }

    /**
     * Registers the high-importance "schedule_reminders" channel that reminder
     * notifications are posted to. Creating an existing channel again is a no-op.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "schedule_reminders",
            "Ders Hatirlaticilari",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ders baslamadan once bildirim gonderir."
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
