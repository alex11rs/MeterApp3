package ru.pepega.meterapp3.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.MainActivity
import ru.pepega.meterapp3.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun isValidReminderTime(value: String): Boolean {
    val parts = value.split(":")
    if (parts.size != 2) return false
    val hour = parts[0].toIntOrNull() ?: return false
    val minute = parts[1].toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

internal fun findNextReminderDate(
    appPreferences: AppPreferences,
    time: String,
    from: Calendar = Calendar.getInstance()
): Calendar? {
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    val reminderSettings = appPreferences.reminderSettings()
    val rawDayFrom = reminderSettings.dayFrom ?: return null
    val rawDayTo = reminderSettings.dayTo ?: return null
    if (rawDayFrom !in 1..31 || rawDayTo !in 1..31) return null
    val dayFrom = rawDayFrom.coerceIn(1, 31)
    val dayTo = rawDayTo.coerceIn(dayFrom, 31)

    repeat(12) { monthOffset ->
        val monthStart = (from.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (appPreferences.isMonthSubmitted(monthStart)) {
            return@repeat
        }
        val maxDay = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
        val rangeEnd = dayTo.coerceAtMost(maxDay)
        if (dayFrom > rangeEnd) {
            return@repeat
        }

        for (day in dayFrom..rangeEnd) {
            val candidate = (monthStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
            }
            if (!candidate.before(from)) {
                return candidate
            }
        }
    }

    return null
}

internal fun monthKey(calendar: Calendar): String {
    return SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(calendar.time)
}

internal fun enqueueReminderWork(
    context: Context,
    workNamePrefix: String,
    index: Int,
    triggerAt: Calendar
) {
    val delayMillis = maxOf(triggerAt.timeInMillis - System.currentTimeMillis(), 1_000L)
    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("notification_index" to index))
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "$workNamePrefix$index",
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}

internal fun ensureReminderNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "meter_reminder",
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            enableVibration(true)
            setSound(
                android.media.RingtoneManager.getDefaultUri(
                    android.media.RingtoneManager.TYPE_NOTIFICATION
                ),
                null
            )
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}

internal fun buildReminderNotification(
    context: Context,
    index: Int
): NotificationCompat.Builder {
    val openPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val submittedPendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        Intent(ReminderActionReceiver.ACTION_MARK_SUBMITTED).apply {
            setPackage(context.packageName)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val title = if (index == 0) {
        context.getString(R.string.reminder_notification_title_first)
    } else {
        context.getString(R.string.reminder_notification_title_repeat)
    }

    val text = if (index == 0) {
        context.getString(R.string.reminder_notification_text_first)
    } else {
        context.getString(R.string.reminder_notification_text_repeat)
    }

    return NotificationCompat.Builder(context, "meter_reminder")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(openPendingIntent)
        .addAction(
            0,
            context.getString(R.string.reminder_action_open),
            openPendingIntent
        )
        .addAction(
            0,
            context.getString(R.string.reminder_action_submitted),
            submittedPendingIntent
        )
        .setAutoCancel(false)
        .setVibrate(longArrayOf(0, 500, 200, 500))
}
