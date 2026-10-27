package eng.nookipet.lab7

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "pet_reminder_channel"
    private const val CHANNEL_NAME = "Pet care reminders"

    // Creates the notification channel. Required on Android 8.0+ (API 26).
    // Safe to call multiple times — if the channel exists, nothing happens.
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders about pet care: feeding, misting, terrarium check"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Builds and shows a notification in the system tray.
    fun showNotification(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // notification disappears after tap
            .build()

        // notify() requires POST_NOTIFICATIONS permission on Android 13+
        // We check it here to avoid SecurityException.
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), notification)
            }
        } catch (e: SecurityException) {
            // Permission not granted — silently skip.
            // In real app you would request it in the UI.
        }
    }
}