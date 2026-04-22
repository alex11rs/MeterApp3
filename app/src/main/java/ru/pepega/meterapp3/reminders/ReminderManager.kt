package ru.pepega.meterapp3.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.R

class ReminderManager(private val context: Context, private val appPreferences: AppPreferences) {
    companion object {
        private const val WORK_NAME_PREFIX = "meter_reminder_"
    }

    fun openAutoStartSettings(activity: ComponentActivity) {
        val intent = Intent()
        val manufacturer = Build.MANUFACTURER.lowercase()

        when {
            manufacturer.contains("xiaomi") ||
                manufacturer.contains("redmi") ||
                manufacturer.contains("poco") -> {
                intent.action = "miui.intent.action.APP_PERM_EDITOR"
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            else -> {
                showXiaomiInstructions(activity)
                return
            }
        }

        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            showXiaomiInstructions(activity)
        }
    }

    fun openAppNotificationSettings(activity: ComponentActivity) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        }
        activity.startActivity(intent)
    }

    fun refreshReminderSchedule() {
        scheduleReminder()
    }

    fun scheduleReminder() {
        cancelReminder()

        if (!appPreferences.reminderSettings().enabled) {
            return
        }

        getReminderTimes().forEachIndexed { index, time ->
            scheduleReminderSlot(index, time)
        }
    }

    fun openBatterySettings(activity: ComponentActivity) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            showBatteryOptimizationInstructions(activity)
        }
    }

    fun cancelReminder() {
        WorkManager.getInstance(context).cancelUniqueWork("${WORK_NAME_PREFIX}0")
        WorkManager.getInstance(context).cancelUniqueWork("${WORK_NAME_PREFIX}1")
    }

    fun markCurrentMonthSubmitted() {
        appPreferences.markCurrentMonthSubmitted()
        refreshReminderSchedule()
    }

    fun isCurrentMonthSubmitted(): Boolean {
        return appPreferences.isCurrentMonthSubmitted()
    }

    fun resetCurrentMonthSubmitted() {
        appPreferences.resetCurrentMonthSubmitted()
        refreshReminderSchedule()
    }

    private fun getReminderTimes(): List<String> {
        return appPreferences.getDistinctReminderTimes()
    }

    private fun scheduleReminderSlot(index: Int, time: String) {
        val nextTrigger = findNextReminderDate(appPreferences, time) ?: return
        enqueueReminderWork(context, WORK_NAME_PREFIX, index, nextTrigger)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    fun isBatteryOptimizationDisabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    fun requestDisableBatteryOptimizations(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isBatteryOptimizationDisabled()) {
                try {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}")
                    )
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    showBatteryOptimizationInstructions(activity)
                }
            }
        }
    }

    fun isXiaomiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer.contains("xiaomi") ||
            manufacturer.contains("redmi") ||
            manufacturer.contains("poco") ||
            brand.contains("xiaomi") ||
            brand.contains("redmi") ||
            brand.contains("poco")
    }

    fun showXiaomiInstructions(activity: ComponentActivity) {
        android.app.AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.xiaomi_setup_title))
            .setMessage(activity.getString(R.string.xiaomi_setup_message))
            .setPositiveButton(activity.getString(R.string.understand), null)
            .setNegativeButton(activity.getString(R.string.open_battery_settings)) { _, _ ->
                requestDisableBatteryOptimizations(activity)
            }
            .show()
    }

    fun showBatteryOptimizationInstructions(activity: ComponentActivity) {
        android.app.AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.battery_optimization_title_plain))
            .setMessage(activity.getString(R.string.battery_optimization_message_plain))
            .setPositiveButton(activity.getString(R.string.disable_action)) { _, _ ->
                requestDisableBatteryOptimizations(activity)
            }
            .setNegativeButton(activity.getString(R.string.later), null)
            .show()
    }

    fun isAutoStartEnabled(): Boolean {
        if (!isXiaomiDevice()) return true

        return try {
            val uri = Uri.parse("content://settings/secure/auto_start_whitelist")
            val cursor = context.contentResolver.query(uri, null, null, null, null)

            var enabled = false
            cursor?.use {
                val columnIndex = cursor.getColumnIndex("value")
                while (cursor.moveToNext()) {
                    val value = cursor.getString(columnIndex)
                    if (value.contains(context.packageName)) {
                        enabled = true
                        break
                    }
                }
            }
            enabled
        } catch (e: Exception) {
            false
        }
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val index = inputData.getInt("notification_index", 0)
        val context = applicationContext
        val appPreferences = AppPreferences.from(context)
        val reminderManager = ReminderManager(context, appPreferences)
        if (!appPreferences.reminderSettings().enabled) {
            return Result.success()
        }
        if (reminderManager.isCurrentMonthSubmitted()) {
            reminderManager.refreshReminderSchedule()
            return Result.success()
        }

        ensureReminderNotificationChannel(context)
        val notification = buildReminderNotification(context, index).build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(1001 + index, notification)

        reminderManager.refreshReminderSchedule()
        return Result.success()
    }
}
