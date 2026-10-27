package eng.nookipet.lab7

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Read the reminder text passed from the enqueue side.
        // "Check terrarium" is the fallback if nothing was provided.
        val reminderText = inputData.getString("reminder_text") ?: "Check terrarium"

        // Show the notification. This runs on a background thread,
        // so we don't block the main thread.
        NotificationHelper.showNotification(
            applicationContext,
            "NookiPet reminder",
            reminderText
        )

        return Result.success()
    }
}