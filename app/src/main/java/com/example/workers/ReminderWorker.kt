package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import com.example.utils.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val settings = db.appSettingsDao().getSettingsDirect()
            if (settings != null && !settings.remindersEnabled) {
                return Result.success()
            }

            val reminders = db.reminderDao().getEnabledReminders()
            val now = System.currentTimeMillis()

            for (reminder in reminders) {
                if (reminder.timestamp <= now) {
                    // Trigger notification
                    NotificationHelper.showReminderNotification(
                        context = context,
                        reminderId = reminder.id,
                        title = reminder.title,
                        description = reminder.description
                    )

                    // Handle repeats
                    when (reminder.repeatOption) {
                        "DAILY" -> {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = reminder.timestamp
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            db.reminderDao().updateReminder(reminder.copy(timestamp = cal.timeInMillis))
                        }
                        "WEEKLY" -> {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = reminder.timestamp
                                add(Calendar.WEEK_OF_YEAR, 1)
                            }
                            db.reminderDao().updateReminder(reminder.copy(timestamp = cal.timeInMillis))
                        }
                        "MONTHLY" -> {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = reminder.timestamp
                                add(Calendar.MONTH, 1)
                            }
                            db.reminderDao().updateReminder(reminder.copy(timestamp = cal.timeInMillis))
                        }
                        else -> { // "ONCE"
                            db.reminderDao().updateReminder(reminder.copy(isEnabled = false))
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val REMINDER_WORK_NAME = "PeriodicReminderWork"

        fun schedule(context: Context) {
            try {
                val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    REMINDER_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } catch (e: Exception) {
                // Ignore in test environments where WorkManager is not initialized
            }
        }
    }
}
