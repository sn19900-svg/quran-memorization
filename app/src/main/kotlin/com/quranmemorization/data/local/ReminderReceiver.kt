package com.quranmemorization.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quranmemorization.MainActivity

/**
 * ReminderReceiver — receives the daily AlarmManager broadcast and posts
 * a Quran memorization reminder notification.
 *
 * Channel:  QURAN_DAILY_REMINDER  (importance HIGH, shown in lock screen)
 * Content:  Rotates through a set of Arabic motivational reminders.
 *
 * To schedule the daily alarm, call [ReminderReceiver.scheduleDailyReminder]
 * from the app's settings screen or on first launch.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID      = "quran_daily_reminder"
        const val NOTIFICATION_ID = 1001
        const val ACTION_DAILY    = "com.quranmemorization.DAILY_REMINDER"

        private val REMINDER_TEXTS = arrayOf(
            "حان وقت حفظك اليومي 📖 لا تنسَ وردك مع القرآن الكريم",
            "خير جليس في الزمان كتاب — ابدأ حفظك الآن 🌟",
            "كل يوم خطوة نحو إتمام الحفظ — تذكّر وردك اليوم",
            "ورد اليوم ينتظرك — استثمر دقائقك مع كتاب الله",
            "مَن حفظ القرآن أُعطي النبوة بين جنبيه — لا تُؤخّر وردك",
        )

        /** Call once to register the daily 8 AM alarm. */
        fun scheduleDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_DAILY
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            // Schedule for 8:00 AM daily, repeating
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                // If 8 AM today has already passed, start tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setRepeating(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                android.app.AlarmManager.INTERVAL_DAY,
                pendingIntent,
            )
        }

        /** Create the notification channel — safe to call multiple times. */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "تذكير الحفظ اليومي",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description        = "تذكير يومي بموعد ورد حفظ القرآن الكريم"
                    enableVibration    = true
                    enableLights(true)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY) return

        // Pick a rotating reminder text
        val reminderIndex = (System.currentTimeMillis() / 86_400_000L % REMINDER_TEXTS.size).toInt()
        val reminderText  = REMINDER_TEXTS[reminderIndex]

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)   // replace with app icon in production
            .setContentTitle("مُذكِّري — ورد القرآن اليومي")
            .setContentText(reminderText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapPending)
            .setAutoCancel(true)
            .build()

        // Check POST_NOTIFICATIONS permission (required on Android 13+)
        val canPost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (canPost) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }
}
