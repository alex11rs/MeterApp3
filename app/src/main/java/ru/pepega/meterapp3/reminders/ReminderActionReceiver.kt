package ru.pepega.meterapp3.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_SUBMITTED = "ru.pepega.meterapp3.ACTION_MARK_SUBMITTED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_SUBMITTED) return
        val appPreferences = ru.pepega.meterapp3.AppPreferences.from(context)
        val reminderManager = ReminderManager(context, appPreferences)
        appPreferences.markCurrentMonthSubmitted()
        val nm = context.getSystemService(NotificationManager::class.java)
        nm?.cancel(1001)
        nm?.cancel(1002)
        reminderManager.refreshReminderSchedule()
    }
}
