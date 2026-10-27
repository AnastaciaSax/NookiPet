package eng.nookipet.lab7.ui.event

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eng.nookipet.lab7.ReminderWorker
import eng.nookipet.lab7.data.entity.Event
import eng.nookipet.lab7.data.entity.EventType
import eng.nookipet.lab7.data.entity.Pet
import eng.nookipet.lab7.data.repository.EventRepository
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddEventViewModel(
    private val selectedPetRepo: SelectedPetRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    val pet: StateFlow<Pet?> = selectedPetRepo.selected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Saves event for the current pet.
     * @param onNoPet called if no pet exists yet
     * @param onDone called after successful save
     */
    fun saveEvent(
        type: EventType,
        title: String,
        amount: String,
        dateLabel: String,
        repeatMode: String,
        notifyInSeconds: Long,
        appContext: Context,
        onNoPet: () -> Unit,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val current = selectedPetRepo.selected.first()
            if (current == null) {
                onNoPet()
                return@launch
            }

            eventRepo.insert(
                Event(
                    petId = current.id,
                    type = type.name,
                    title = title.ifBlank { type.label },
                    amount = amount,
                    repeatMode = repeatMode,
                    timestamp = parseDateLabel(dateLabel)
                )
            )

            if (notifyInSeconds > 0) {
                scheduleNotification(
                    appContext,
                    "${type.label} for ${current.name}",
                    "$dateLabel · $repeatMode",
                    notifyInSeconds
                )
            }
            onDone()
        }
    }

    private fun parseDateLabel(label: String): Long {
        return try {
            val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val parsed = fmt.parse(label) ?: return System.currentTimeMillis()
            val parsedCal = Calendar.getInstance().apply { time = parsed }
            val nowCal = Calendar.getInstance()

            val sameDay = parsedCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    parsedCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

            if (sameDay) System.currentTimeMillis()
            else parsedCal.apply {
                set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun scheduleNotification(
        appContext: Context,
        title: String,
        body: String,
        delaySeconds: Long
    ) {
        val data = Data.Builder()
            .putString("reminder_text", "$title — $body")
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueue(request)
    }
}