package eng.nookipet.lab7.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eng.nookipet.lab7.ReminderWorker
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.entity.Reminder
import eng.nookipet.lab7.data.repository.ReminderRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * Backs the Reminders screen.
 *
 * WorkManager tasks are now unique per reminder ("reminder_{id}"):
 *   - ON / create → enqueueUniqueWork(REPLACE) — no duplicates
 *   - OFF / delete → cancelUniqueWork — kills pending notification
 *
 * If the reminder time is already past for today, the notification
 * is scheduled for tomorrow at the same hour:minute.
 */
class RemindersViewModel(
    app: Application,
    private val selectedPetRepo: SelectedPetRepository,
    private val reminderRepo: ReminderRepository
) : AndroidViewModel(app) {

    private val appContext = app.applicationContext

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reminders: StateFlow<List<Reminder>> = selectedPetRepo.selected
        .flatMapLatest { p ->
            flow {
                if (p == null) emit(emptyList())
                else reminderRepo.getForPet(p.id).collect { emit(it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggle(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            reminderRepo.setEnabled(id, enabled)
            if (enabled) {
                reminders.value.firstOrNull { it.id == id }?.let { scheduleAt(it) }
            } else {
                cancelScheduled(id)
            }
        }
    }

    fun addReminder(
        title: String,
        scheduleLabel: String,
        type: String,
        hour: Int,
        minute: Int,
        onNoPet: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = selectedPetRepo.selected.first()
            if (current == null) {
                onNoPet()
                return@launch
            }
            val id = reminderRepo.insert(
                Reminder(
                    petId = current.id,
                    type = type,
                    title = title,
                    scheduleLabel = scheduleLabel,
                    hourOfDay = hour,
                    minute = minute
                )
            )
            scheduleAt(id, hour, minute, title)
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            cancelScheduled(reminder.id)
            reminderRepo.delete(reminder)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // WorkManager
    // ──────────────────────────────────────────────────────────────

    private fun scheduleAt(reminder: Reminder) =
        scheduleAt(reminder.id, reminder.hourOfDay, reminder.minute, reminder.title)

    private fun scheduleAt(id: Long, hour: Int, minute: Int, title: String) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If less than 30 seconds away, push to tomorrow — WorkManager
        // does not guarantee immediate execution, so we always stay in the future.
        if (target.timeInMillis - now.timeInMillis < 30_000L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delaySeconds = (target.timeInMillis - now.timeInMillis) / 1000

        val data = Data.Builder()
            .putString("reminder_text", "$title — scheduled reminder")
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("reminder_$id", ExistingWorkPolicy.REPLACE, request)
    }

    private fun cancelScheduled(id: Long) {
        WorkManager.getInstance(appContext).cancelUniqueWork("reminder_$id")
    }
}