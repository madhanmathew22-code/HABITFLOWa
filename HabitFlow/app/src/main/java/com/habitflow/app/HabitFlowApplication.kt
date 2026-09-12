package com.habitflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Hilt generates the DI container here. Any process-wide, non-DI setup
 * (WorkManager scheduling for reminders, notification channel creation,
 * etc.) should be added in [onCreate] as those pieces are implemented.
 */
@HiltAndroidApp
class HabitFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // TODO(section 19): create notification channels for habit reminders.
        // TODO(section 19): enqueue the daily WorkManager job that schedules
        //   the next 24h of reminder notifications from HabitReminder rows.
    }
}
